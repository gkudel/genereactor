package com.softcomputer.annotationprocessor.reflections;

import org.apache.commons.lang3.Validate;
import org.reflections.Configuration;
import org.reflections.adapters.JavaReflectionAdapter;
import org.reflections.scanners.Scanner;
import org.reflections.util.ClasspathHelper;

import javax.annotation.Nonnull;

public class ConfigurationBuilder {

    private org.reflections.util.ConfigurationBuilder builder;
    public ConfigurationBuilder(String packageName) {
        Validate.notEmpty(packageName, "packageName");
        builder = new org.reflections.util.ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(packageName))
                .setMetadataAdapter(new JavaReflectionAdapter());
    }

    public ConfigurationBuilder setScanners(@Nonnull Scanner... scanners) {
        builder.setScanners(scanners);
        return this;
    }

    protected Configuration build() {
        return builder;
    }
}
