package com.checkmarxts.cicd.github;


public class CxFlowUrlResolverFactory {

    public static CxFlowUrlResolver forLatest()
    {
        return new CxFlowUrlResolver("/repos/checkmarx-ltd/cx-flow/releases/latest");
    }

    public static CxFlowUrlResolver forTag(String tag)
    {
        return new CxFlowUrlResolver(String.format("/repos/checkmarx-ltd/cx-flow/releases/tags/%s", tag));
    }
    
}
