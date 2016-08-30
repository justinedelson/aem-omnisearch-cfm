<%--
  ADOBE CONFIDENTIAL

  Copyright 2014 Adobe Systems Incorporated
  All Rights Reserved.

  NOTICE:  All information contained herein is, and remains
  the property of Adobe Systems Incorporated and its suppliers,
  if any.  The intellectual and technical concepts contained
  herein are proprietary to Adobe Systems Incorporated and its
  suppliers and may be covered by U.S. and Foreign Patents,
  patents in process, and are protected by trade secret or copyright law.
  Dissemination of this information or reproduction of this material
  is strictly forbidden unless prior written permission is obtained
  from Adobe Systems Incorporated.
--%><%
%><%@include file="/libs/granite/ui/global.jsp"%><%
%><%@page session="false"
          import="org.apache.commons.lang.StringUtils,
                  com.adobe.granite.ui.components.ds.DataSource,
                  com.adobe.granite.ui.components.ds.ResourceDataSource,
                  com.adobe.granite.ui.components.ds.EmptyDataSource,
                  com.adobe.granite.omnisearch.api.core.OmniSearchService"%><%

    final String location = request.getParameter("location");

    DataSource ds = EmptyDataSource.instance();

    if (!StringUtils.isBlank(location)) {
        Resource configResource = sling.getService(OmniSearchService.class).getModuleConfiguration(resourceResolver, location);
        if (configResource != null) {
            String availableColumnsResourcePath = configResource.getPath() + "/views/list";
            Resource availableColumns = resourceResolver.getResource(availableColumnsResourcePath + "/columns");

            if (availableColumns == null) {
                availableColumns = resourceResolver.getResource(availableColumnsResourcePath + "/columnsdatasource");
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