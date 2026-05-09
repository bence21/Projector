<div>
  <h3>Új verzió összekötés<#if songLinkRows?has_content && songLinkRows?size gt 1>ek</#if>:</h3>
<#if songLinkRows?has_content>
  <#list songLinkRows as row>
  <div style="margin-bottom: 1.5em;">
<#--noinspection FtlReferencesInspection-->
    <a href="${baseUrl}/#/song/${row.song1Uuid}">${row.song1Title}</a>
    <br>
<#--noinspection FtlReferencesInspection-->
    <a href="${baseUrl}/#/song/${row.song2Uuid}">${row.song2Title}</a>
<#--noinspection FtlReferencesInspection-->
    <h3>Email: </h3><h4>${row.email}</h4>
    <br>
    <h3><a href="${baseUrl}/#/songLink/${row.id}">Link</a></h3>
  </div>
  </#list>
</#if>
</div>
