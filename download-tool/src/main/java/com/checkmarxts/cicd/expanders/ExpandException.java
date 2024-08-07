package com.checkmarxts.cicd.expanders;
import java.lang.Throwable;


public class ExpandException extends Exception {

    public ExpandException(Throwable outer, String path)
    {
        super(String.format("Error expanding [%s]: %s", path, outer.getMessage()));
    }

    public ExpandException(Throwable outer)
    {
        super(outer);
    }
    
}
