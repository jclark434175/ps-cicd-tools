package com.checkmarxts.cicd.expanders;

import java.lang.AutoCloseable;
import java.io.InputStream;
import java.nio.file.Path;

public interface IExpandedWriter extends AutoCloseable {
    void expand(InputStream src) throws ExpandException;
    IExpandedWriter getInstance(Path destpath) throws ExpandException;
}
