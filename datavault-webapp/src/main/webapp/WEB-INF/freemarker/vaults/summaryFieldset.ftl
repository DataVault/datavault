

<fieldset>
   <div class="form-card">
      <h2 class="fs-title">Summary</h2>
      <div>
         <table class="table table-sm">
            <tbody>
               <tr>
                  <th scope="col">Vault Name</th>
                  <td>
                  <@spring.bind "vault.name">
                      <span name="name" name="${spring.status.expression}">${spring.status.value!""}"</span>
                  </@spring.bind>
                  </td>
               </tr>
               
               <tr>
                  <th scope="col">Creation Date</th>
                  <td>
                     <#if (vault.creationTime)??>
                     ${vault.creationTime?date}
                     </#if>
                  </td>
               </tr>
               <tr>
                  <th scope="col">Description</th>
                  <td> 
                     <#if (vault.description)??>
                     ${vault.description?html}
                     </#if>
                  </td>
               </tr>
               <tr>
                  <th scope="col">Billing Type</td>
                  <td>
                     <#if (vault.billingType)??>
                     ${vault.billingType?html}
                     </#if>
                  </td>
               </tr>
               <tr>
                  <th scope="col">Billing Notes</td>
                  <td>
                     <#if (vault.notes)??>
                     ${vault.notes?html}
                     </#if>
                  </td>
               </tr>
               <tr>
                  <th scope="col">Estimate Size</td>
                  <td>
                     <#if (vault.estimate)??>
                     ${vault.estimate?html}
                     </#if>
                  </td>
               </tr>
               <tr>
                  <th scope="col">School or Unit</td>
                  <td>
                     <#if (vault.schoolOrUnit)??>
                       ${vault.schoolOrUnit?html}
                     </#if>
                  </td>
               </tr>
               <tr>
                  <th scope="col">Subunit</td>
                  <td>
                     <#if (vault.subunit)??>
                       ${vault.subunit?html}
                     </#if>
                  </td>
               </tr>
               <tr>
                  <th scope="col">Project ID</td>
                  <td>
                     <#if (vault.projectID)??>
                       ${vault.projectID?html}
                     </#if>
                  </td>
               </tr>
               <tr>
                  <th scope="col">Slice ID</td>
                  <td>
                     <#if (vault.sliceID)??>
                        ${vault.sliceID?html}
                     </#if>
                  </td>
               </tr>
               <tr>
                  <th scope="col">RetentionPolicy</td>
                  <td>
                     <#if (vault.retentionPolicy.getName())??>
                     ${vault.retentionPolicy.getName()?html}
                     </#if>
                  </td>
               </tr>
               <tr>
                  <th scope="col">Group</td>
                  <td>
                     <#if (vault.group.getName())??>
                     ${vault.group.getName()?html}
                     </#if>
                  </td>
               </tr>
               <tr>
                  <th scope="col">Grant End Date</td>
                  <td>
                     <#if (vault.grantEndDate)??>
                     ${vault.grantEndDate?date}
                     </#if>
                  </td>
               </tr>
               <tr>
                  <th scope="col">Review Date</td>
                  <td>
                     <#if (vault.reviewDate)??>
                     ${vault.reviewDate?date}" 
                     </#if>
                  </td>
               </tr>
            </tbody>
         </table>
      </div>
      <div>
         Please make sure all information above are correct and click on confirm to process to your vault creation.
      </div>
      <div class="alert alert-warning">
         <strong>PLEASE NOTE – this information will be public, and will be linked to the PI’s Pure profile.</strong>
         <div class="checkbox">
            <label>
            <input id="affirmation-check" type="checkbox"> I agree
            </label>
         </div>
      </div>
   </div>
   <button type="button" name="previous" class="previous action-button-previous btn btn-default" >&laquo; Previous</button>
   <button type="submit" name="save" value="Save" class="save action-button-previous btn btn-default" >
   <span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span> Save
   </button>
   <button type="submit" name="confirm" value="Confirm" class="next action-button btn btn-success">
   <span class="glyphicon glyphicon-ok" aria-hidden="true"></span> Confirm
   </button>
</fieldset>