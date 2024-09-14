<table align="center" height="100%" width="100%"
  style="border-collapse: collapse;height: 100%;margin: 0;width: 100%;background-color: rgb(218, 218, 218);">
  <tr>
    <td align="center" valign="top" style="height: 100%;margin: 0;padding: 10px;width: 100%;">
      <table width="100%" style="border-collapse: collapse;border: 0;max-width: 100% !important;">
        <tr>
          <td valign="top" style="background-color: rgb(7, 72, 1);padding-top: 9px;padding-bottom: 0;">
          </td>
        </tr>
        <tr>
          <td valign="top"
            style="background-color: rgb(7, 72, 1);color:#ffffff; background-position: center;background-size: cover;padding-bottom: 9px;">
            <table width="100%" style="min-width: 100%;border-collapse: collapse;">
              <tbody>
                <tr>
                  <td valign="top">
                    <table align="left" width="100%" style="min-width: 100%;border-collapse: collapse;">
                      <tbody>
                        <tr>
                          <td style="padding-top: 9px;padding-left: 18px;padding-bottom: 9px;padding-right: 18px;">
                            <table width="100%"
                              style="min-width: 100% !important;background-color: rgb(25, 60, 22);border-collapse: collapse;">
                              <tbody>
                                <tr>
                                  <td valign="top" style="padding: 18px;color: #ffffff;">
                                    <h1
                                      style="text-align: center;display: block;margin: 0;color: #ffffff;font-family: 'Source Sans Pro', 'Helvetica Neue', Helvetica, Arial, sans-serif;font-size: 21px;font-style: normal;font-weight: bold;line-height: 125%;letter-spacing: normal;">
                                      There is songs that are empty!</h1>
                                    <p
                                      style="text-align: center;display: block;margin: 0;color: #ffffff;font-family: 'Source Sans Pro', 'Helvetica Neue', Helvetica, Arial, sans-serif;font-size: 12px;font-style: normal;line-height: 225%;letter-spacing: normal;">
                                      Please check
                                    </p>
                                  </td>
                                </tr>
                                <tr>
                                  <td valign="top" style="padding: 18px;color: #ffffff;">
                                    <p
                                      style="text-align: center;display: block;margin: 0;color: #ffffff;font-family: 'Source Sans Pro', 'Helvetica Neue', Helvetica, Arial, sans-serif;font-size: 12px;font-style: normal;line-height: 225%;letter-spacing: normal;">
                                      <a href="${baseUrl}/admin/checkForEmptySongs" target="_blank"
                                          style="color: #2BAADF;font-weight: normal;text-decoration: underline;">${baseUrl}/admin/checkForEmptySongs</a>
                                    </p>
                                  </td>
                                </tr>
                              </tbody>
                            </table>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </td>
                </tr>
              </tbody>
            </table>
            <table width="100%" style="min-width: 100%;border-collapse: collapse;table-layout: fixed !important;">
              <tbody>
                <tr>
                  <td style="min-width: 100%;padding: 9px 18px;">
                    <table width="100%"
                      style="min-width: 100%;border-top: 2px solid rgb(108, 108, 108);border-collapse: collapse;">
                      <tbody>
                        <tr>
                          <td>
                            <span></span>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </td>
                </tr>
              </tbody>
            </table>

            <table width="100%" style="min-width: 100%;border-collapse: collapse;">
              <tbody>
                <tr>
                  <td valign="top" style="padding-top: 9px;">
                    <table align="left" style="max-width: 100%;min-width: 100%;border-collapse: collapse;" width="100%">
                      <tbody>
                        <tr>
                          <td valign="top"
                            style="padding-top: 0;padding-right: 18px;padding-bottom: 9px;padding-left: 18px;word-break: normal;color: #ffffff;font-family: 'Source Sans Pro', 'Helvetica Neue', Helvetica, Arial, sans-serif;font-size: 15px;line-height: 150%;text-align: left;">
                            <p
                              style="margin: 10px 0;color: #ffffff;font-family: 'Source Sans Pro', 'Helvetica Neue', Helvetica, Arial, sans-serif;font-size: 15px;line-height: 150%;text-align: left;">
                              <div>
                                <table style="color: #ffffff; border-spacing: 4px 20px; border-collapse: separate;">
                                  <#list songs as song>
                                    <tr>
                                      <td>
                                        <#if song.title??>
                                          ${song.title}
                                        </#if>
                                      </td>
                                      <td>
                                        <a href="${baseUrl}/#/song/${song.uuid}" target="_blank"
                                          style="color: #2BAADF;font-weight: normal;text-decoration: underline;">Open</a>
                                      </td>
                                    </tr>
                                  </#list>
                                </table>
                              </div>
                            </p>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </td>
                </tr>
              </tbody>
            </table>
            <table width="100%" style="min-width: 100%;border-collapse: collapse;table-layout: fixed !important;">
              <tbody>
                <tr>
                  <td style="min-width: 100%;padding: 9px 18px;">
                    <table width="100%"
                      style="min-width: 100%;border-top: 2px solid rgb(108, 108, 108);border-collapse: collapse;">
                      <tbody>
                        <tr>
                          <td>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </td>
                </tr>
              </tbody>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>