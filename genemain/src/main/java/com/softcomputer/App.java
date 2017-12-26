package com.softcomputer;

import com.softcomputer.factory.Factories;
import com.softcomputer.factory.Factory;
import com.softcomputer.model.TestNgs;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
        Factory<TestNgs>  factory = Factories.get(TestNgs.class);
    }
}
