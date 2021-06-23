<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<#assign sec=JspTaglibs["http://www.springframework.org/security/tags"] />
<@layout.vaultLayout>

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/pendingVaults"><b>Pending Vaults</b></a></li>
        <li class="active"><b>Pending Vault Summary</b></li>
    </ol>
    
     

  <h1>Pending Vault Summary</h1>
  
  
  <a name="previous" class="btn btn-primary" 
      href="${springMacroRequestContext.getContextPath()}/admin/pendingVaults">&laquo; Return</a>
     
  
 <form id="create-vault" class="form" role="form" action="${springMacroRequestContext.getContextPath()}/admin/pendingVaults/addVault/${pendingVault.id}" method="get" novalidate="novalidate" _lpchecked="1">
    <div class="table-responsive" id="pendingVaultsTable">
         <table class="table table-striped">
                <thead>
                    <tr class="tr">
                       <th>Data Name</th>     
                       <th>Data</th>
                    </tr>
                </thead>

                
  
      <tbody>
    <tr class="tr">
      <td>Vault Name</td>
      <td>${pendingVault.name?html}</td>
    </tr>
    <tr class="tr">
      <td>Creator(UUN) - TO BE CLARIFIED</td>
       <td>
         <#if (pendingVault.user)??>
             ${pendingVault.user.getFirstname()?html} ${pendingVault.user.getLastname()?html} (${pendingVault.user.getID()?html}) 
         </#if>
       </td>
    </tr>
    <td>Owner(UUN) - TO BE CLARIFIED</td>
       <td>
         <#if (pendingVault.user)??>
             ${pendingVault.user.getFirstname()?html} ${pendingVault.user.getLastname()?html} (${pendingVault.user.getID()?html}) 
         </#if>
       </td>
    </tr>
    
    <tr class="tr">
      <td>Creation Date</td>
      <td>
         <#if (pendingVault.creationTime)??>
           ${pendingVault.creationTime?date}
         </#if>
      </td>
    </tr>
    <tr class="tr">
      <td>Description</td>
      <td>
         <#if (pendingVault.description)??>
           ${pendingVault.description?html}
         </#if>
      </td>
    </tr>
    <tr class="tr">
      <td>Billing Type</td>
      <td>
         <#if (pendingVault.billingType)??>
           ${pendingVault.billingType?html}
         </#if>
      </td>
    </tr>
    <tr class="tr">
      <td>Billing Notes</td>
      <td>
         <#if (pendingVault.notes)??>
           ${pendingVault.notes?html}
         </#if>
      </td>
    </tr>
    <tr class="tr">
      <td>Estimate</td>
      <td>
         <#if (pendingVault.estimate)??>
           ${pendingVault.estimate?html}
         </#if>
      </td>
    </tr>
    <tr class="tr">
      <td>School or Unit</td>
       <td>
         <#if (pendingVault.schoolOrUnit)??>
           ${pendingVault.schoolOrUnit?html}
         </#if>
      </td>
    </tr>
    <tr class="tr">
      <td>Subunit</td>
      <td>
         <#if (pendingVault.subunit)??>
           ${pendingVault.subunit?html}
         </#if>
      </td>
    </tr>
     <tr class="tr">
      <td>Project ID</td>
      <td>
         <#if (pendingVault.projectID)??>
           ${pendingVault.projectID?html}
         </#if>
      </td>
    </tr>
    <tr class="tr">
      <td>Slice ID</td>
      <td>
         <#if (pendingVault.sliceID)??>
           ${pendingVault.sliceID?html}
         </#if>
      </td>
    </tr>
    <tr class="tr">
      <td>RetentionPolicy</td>
      <td>
         <#if (pendingVault.retentionPolicy.getName())??>
           ${pendingVault.retentionPolicy.getName()?html}
         </#if>
      </td>
    </tr>
    <tr class="tr">
      <td>Group</td>
      <td>
         <#if (pendingVault.group.getName())??>
           ${pendingVault.group.getName()?html}
         </#if>
      </td>
    </tr>
    <tr class="tr">
      <td>Grant End Date</td>
      <td>
         <#if (pendingVault.grantEndDate)??>
           ${pendingVault.grantEndDate?date}
         </#if>
      </td>
    </tr>
   <tr class="tr">
      <td>Review Date</td>
      <td>
        
         <#if (pendingVault.reviewDate)??>
           <input id="reviewDate" name="reviewDate" 
           class="form-control date-picker" 
           value="${pendingVault.reviewDate?date}" 
           placeholder="Select date from calendar by clicking in input box"/>
         <#else>
           <input id="reviewDate" name="reviewDate" 
           class="form-control date-picker" placeholder="Select date from calendar by clicking in input box"/>
         </#if>
         
      </td>
    </tr>
    
  </tbody>
</table>
</div>

 <div class="container">
   <div class="col-md-12 text-center">
     <#if (pendingVault.id)??>
          <a name="delete-pending-vault" class="btn btn-default" 
          href="${springMacroRequestContext.getContextPath()}/admin/pendingVaults"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span>Cancel</a>
       

          <a name="delete-pending-vault" class="btn btn-danger" 
          href="${springMacroRequestContext.getContextPath()}/admin/pendingVaults/delete/${pendingVault.id}"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span> Delete Pending Vault</a>
       
         
         <button type="submit" value="submit" class="btn btn-success">
              <span class="glyphicon glyphicon-folder-close"></span>
                                    Create Vault
         </button>
         
        
     </#if>
     
  </div>
</div>
<form>
                            
</div>

<script>
$(document).ready(function () {
    $.datepicker.setDefaults({
        dateFormat: "dd-M-yy",
        changeMonth: true,
        changeYear: true,
        showOtherMonths: true,
        selectOtherMonths: true
    });
     
    
    $("#reviewDate").datepicker({
       minDate: '+1m'
    });
    

     $.validator.addMethod(
                "reviewDate",
                function(value, element) {
                    // put your own logic here, this is just a (crappy) example

                    if (value){
                        var inAMonth = new Date();
                        inAMonth.setMonth(inAMonth.getMonth() + 1);
                        inAMonth.setHours( 0,0,0,0 );

                        var selectedDate = new Date(value);

                        return selectedDate >= inAMonth;
                    } else {
                        return true;
                    }
                },
                "The review date must be at least one month in the future."
     );
     
     $('#create-vault').validate({
                debug: true,
                rules: {
              
                    reviewDate : {
                        required: true,
                        date: true,
                        reviewDate: true
                    }
                },
                highlight: function (element) {
                    $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
                },
                success: function (element) {
                    element.addClass('valid')
                        .closest('.form-group').removeClass('has-error').addClass('has-success');
                },
                submitHandler: function (form) {
                    $('button[type="submit"]').prop('disabled', true);
                    form.submit();
                }
            });
     
});
</script>
</@layout.vaultLayout>
       
  
 
