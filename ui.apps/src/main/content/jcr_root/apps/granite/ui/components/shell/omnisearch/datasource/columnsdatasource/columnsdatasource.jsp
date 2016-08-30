<%@include file="/libs/granite/ui/global.jsp"%><%
%><%@page session="false"
          import="org.apache.commons.lang.StringUtils,
                  com.adobe.granite.ui.components.ds.DataSource,
                  com.adobe.granite.ui.components.ds.ResourceDataSource,
                  com.adobe.granite.ui.components.ds.EmptyDataSource,
                  com.adobe.granite.omnisearch.api.core.OmniSearchService"%><%

    final String location = request.getParameter("location");
    DataSource ds = EmptyDataSource.instance();

    if (!StringUtils.isBlank(location)) {
        // we need to use the omnisearch service to locate the configuration for the given location
        OmniSearchService searchService = sling.getService(OmniSearchService.class);
        Resource configRes = searchService.getModuleConfiguration(resourceResolver, location);

        if (configRes != null) {
            Resource availableColumns = configRes.getChild("views/list/columns");

            if (availableColumns == null) {
                availableColumns = configRes.getChild("views/list/columnsdatasource");
                if (availableColumns != null) {
                    ds = cmp.asDataSource(availableColumns);
                }
            } else {
                ds = new ResourceDataSource(availableColumns);
            }
        }
    }

    request.setAttribute(DataSource.class.getName(), ds);
%>