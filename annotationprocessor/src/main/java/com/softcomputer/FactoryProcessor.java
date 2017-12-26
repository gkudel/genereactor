package com.softcomputer;

import com.google.auto.service.AutoService;
import com.softcomputer.annotations.Column;
import com.softcomputer.annotations.MetaData;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@SupportedAnnotationTypes("com.softcomputer.annotations.Column")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class FactoryProcessor extends AbstractProcessor {

    private static Map<String, String> FunctionMapping = new HashMap<>();
    static {
        FunctionMapping.put(String.class.getName(), "getString");
        FunctionMapping.put(Long.class.getName(), "getLong");
        FunctionMapping.put(long.class.getName(), "getLong");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<TypeDescriptor> factories = new ArrayList<>();
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            Map<TypeElement, TypeDescriptor> mapping = new HashMap<>();
            for (Element element : annotatedElements) {
                if(element.getKind() != ElementKind.FIELD) throw new UnsupportedOperationException();
                TypeElement typeElement =  ((TypeElement) element.getEnclosingElement());
                if (!mapping.containsKey(typeElement)) mapping.put(typeElement, new TypeDescriptor() {{
                    setType(typeElement);
                    setElements(new ArrayList<>());
                    setNestedTypes(new ArrayList<>());
                }});
                mapping.get(typeElement).getElements().add(element);
            }
            if(mapping.isEmpty()) continue;
            List<TypeElement> toRemove = new ArrayList<>();
            for (TypeElement typeElement : mapping.keySet()) {
                if(typeElement.getSuperclass() != null) {
                    TypeElement superElement = (TypeElement) processingEnv.getTypeUtils().asElement(typeElement.getSuperclass());
                    if(mapping.containsKey(superElement)) {
                        mapping.get(superElement).nestedTypes.add(mapping.get(typeElement));
                        toRemove.add(typeElement);
                    }
                }
                MetaData metaData = typeElement.getAnnotation(MetaData.class);
                if(metaData != null) {
                    TypeMirror metaDataTypeMirror = null;
                    try {
                        metaData.type();
                    } catch (MirroredTypeException e) {
                        metaDataTypeMirror = e.getTypeMirror();
                    }
                    if(metaDataTypeMirror != null) {
                        TypeElement metaDataType = (TypeElement) processingEnv.getTypeUtils().asElement(metaDataTypeMirror);
                        if(mapping.containsKey(metaDataType)) {
                            mapping.get(typeElement).metaDataType = mapping.get(metaDataType);
                            toRemove.add(metaDataType);
                        }
                    }
                }
            }
            for (TypeElement element : toRemove) {
                mapping.remove(element);
            }
            for (TypeElement typeElement: mapping.keySet()) {
                try {
                    writeFactoryFile(mapping.get(typeElement));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private void writeFactoryFile(TypeDescriptor typeDescriptor) throws IOException {
        writeFactoryFile(typeDescriptor, StringUtils.EMPTY);
    }

    private void writeFactoryFile(TypeDescriptor typeDescriptor, String parentFactoryClassName) throws IOException {

        String className = typeDescriptor.type.getQualifiedName().toString();
        String packageName = null;
        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }

        String simpleClassName = className.substring(lastDot + 1);
        String factoryClassName = className + "Factory";
        String factorySimpleClassName = factoryClassName.substring(lastDot + 1);

        JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(factoryClassName);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {

            if (packageName != null) {
                out.print("package ");
                out.print(packageName);
                out.println(";");
                out.println();
            }

            out.println("import com.softcomputer.factory.Factory;");
            out.println("import java.sql.ResultSet;");
            out.println("import java.sql.SQLException;");
            out.println();
            out.print("public class ");
            out.print(factorySimpleClassName);
            out.print(" implements Factory<");
            out.print(className);
            out.print(">");
            out.println(" {");
            out.println();

            if(StringUtils.isNotEmpty(parentFactoryClassName)) {
                out.print("    private ");
                out.print(parentFactoryClassName);
                out.print(" superFactory = new ");
                out.print(parentFactoryClassName);
                out.println("();");
                out.println();
            }

            out.print("    public ");
            out.print(simpleClassName);
            out.println(" create(ResultSet resultSet) throws SQLException {");
            out.print("        " + simpleClassName);
            out.print(" object = new ");
            out.print(simpleClassName);
            out.println("();");
            out.println("        fill(object, resultSet);");
            out.println("        return object;");
            out.println("    }");
            out.println();

            out.print("    protected void fill(");
            out.print(simpleClassName);
            out.println(" object, ResultSet resultSet) throws SQLException {");
            if(StringUtils.isNotEmpty(parentFactoryClassName)) {
                out.println("        superFactory.fill(object, resultSet);");
            }
            if(typeDescriptor.metaDataType != null) writeFillStatements(out, typeDescriptor.metaDataType.elements);
            writeFillStatements(out, typeDescriptor.elements);
            out.println("    }");
            out.println("}");
        }
        if(typeDescriptor.nestedTypes != null) {
            for (TypeDescriptor desc: typeDescriptor.nestedTypes) {
                writeFactoryFile(desc, packageName + "." + factorySimpleClassName);
            }
        }
    }

    private void writeFillStatements(PrintWriter out, List<Element> elements) {
        if(elements != null) {
            for (Element element : elements) {
                out.print("        object.set" + StringUtils.capitalize(element.getSimpleName().toString()));
                out.print("(resultSet.");
                if(!FunctionMapping.containsKey(element.asType().toString())) throw new UnsupportedOperationException();
                out.print(FunctionMapping.get(element.asType().toString()));
                Column column = element.getAnnotation(Column.class);
                if (column == null || StringUtils.isEmpty(column.name())) throw new UnsupportedOperationException();
                out.print("(\"" + column.name() + "\")");
                out.println(");");
            }
        }
    }

    private class TypeDescriptor {
        private TypeElement type;
        private List<Element> elements;
        private List<TypeDescriptor> nestedTypes;
        private TypeDescriptor metaDataType;

        public TypeElement getType() {
            return type;
        }

        public void setType(TypeElement type) {
            this.type = type;
        }

        public List<Element> getElements() {
            return elements;
        }

        public void setElements(List<Element> elements) {
            this.elements = elements;
        }

        public List<TypeDescriptor> getNestedTypes() {
            return nestedTypes;
        }

        public void setNestedTypes(List<TypeDescriptor> nestedTypes) {
            this.nestedTypes = nestedTypes;
        }

        public TypeDescriptor getMetaDataType() {
            return metaDataType;
        }

        public void setMetaDataType(TypeDescriptor metaDataType) {
            this.metaDataType = metaDataType;
        }
    }
}
