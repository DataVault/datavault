<fieldset>
   <div class="form-card">
      <h2 class="fs-title">Summary</h2>
      <div>
         <table class="table table-sm">
            <tbody>
               <tr>
                  <th scope="col">Vault Name</th>
                  <td>
                    <@spring.bind "vault.name" />
                    <span name="${spring.status.expression}">${spring.status.value!""}</span>
                  </td>
               </tr>
               <tr>
                  <th scope="col">Description</th>
                  <td> 
                     <@spring.bind "vault.description" />
                     <span name="${spring.status.expression}">${spring.status.value!""}</span>
                  </td>
               </tr>
               <tr>
                  <th scope="col">Billing Type</td>
                  <td>
                     <@spring.bind "vault.billingType" />
                     <span name="${spring.status.expression}">${spring.status.value!""}</span>
                  </td>
               </tr>
               <tr>
                  <th scope="col">Billing Notes</td>
                  <td>
                     <@spring.bind "vault.notes" />
                     <span name="${spring.status.expression}">${spring.status.value!""}</span>
                  </td>
               </tr>
               <tr>
                  <th scope="col">Estimate Size</td>
                  <td>
                     <@spring.bind "vault.estimate" />
                     <span name="${spring.status.expression}">${spring.status.value!""}</span>
                     
                  </td>
               </tr>
               <tr>
                  <th scope="col">School or Unit</td>
                  <td>
                     <@spring.bind "vault.schoolOrUnit" />
                     <span name="${spring.status.expression}">${spring.status.value!""}</span>
                     
                  </td>
               </tr>
               <tr>
                  <th scope="col">Subunit</td>
                  <td>
                     <@spring.bind "vault.subunit" />
                      <span name="${spring.status.expression}">${spring.status.value!""}</span>
                  </td>
               </tr>
               <tr>
                  <th scope="col">Project ID</td>
                  <td>
                     <@spring.bind "vault.projectID" />
                      <span name="${spring.status.expression}">${spring.status.value!""}</span>
                     
                  </td>
               </tr>
               <tr>
                  <th scope="col">Slice ID</td>
                  <td>
                     <@spring.bind "vault.sliceID" />
                     <span name="${spring.status.expression}">${spring.status.value!""}</span>
                  </td>
               </tr>
     
               <tr>
                  <th scope="col">Grant End Date</td>
                  <td>
                     <@spring.bind "vault.grantEndDate" />
                     <span name="${spring.status.expression}">${spring.status.value!""}</span>
                  </td>
               </tr>
               <tr>
                  <th scope="col">Review Date</td>
                  <td>
                     <@spring.bind "vault.reviewDate" />
                     <span name="${spring.status.expression}">${spring.status.value!""}</span> 
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