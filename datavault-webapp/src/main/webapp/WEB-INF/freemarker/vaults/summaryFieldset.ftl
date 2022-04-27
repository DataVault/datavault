<fieldset id="summary-fieldset">
    <div class="form-card">
        <h2 class="fs-title">Summary</h2>
        <div>
       <table class="table table-sm">
	<tbody>
		<tr>
			<th scope="col">
				<h3 class="fs-title">Vault Information</h3></th>
			<td></td>
		</tr>
		<tr>
			<th scope="col">Affirmation</th>
			<td><span id="summary-affirmation-check"></span> </td>
		</tr>
		<tr>
			<th scope="col">Vault Name</th>
			<td><span id="summary-vaultName"></span> </td>
		</tr>
		<tr>
			<th scope="col">Description</th>
			<td><span id="summary-description"></span> </td>
		</tr>
		<tr>
			<th scope="col">Retention Policy</th>
			<td><span id="summary-policyID"></span> </td>
		</tr>
		<tr>
			<th scope="col">Grant End Date</th>
			<td><span id="summary-grantEndDate"></span> </td>
		</tr>
		<tr>
			<th scope="col">School</th>
			<td><span id="summary-groupID"></span> </td>
		</tr>
		<tr>
			<th scope="col">Review Date</th>
			<td><span id="summary-reviewDate"></span> </td>
		</tr>
		<tr>
			<th scope="col">Estimate of the amount of data</th>
			<td><span id="summary-estimate"></span> </td>
		</tr>
		<tr>
			<th scope="col">Notes regarding data retention</th>
			<td><span id="summary-notes"></span> </td>
		</tr>
		<tr>
			<th scope="col">
				<h3 class="fs-title">Billing</h3></th>
			<td></td>
		</tr>
		<tr>
			<th scope="col">Funding for this vault will be from</th>
			<td><span id="summary-billing-type"></span> </td>
		</tr>
		<tr class="summary-slice-billing-row">
			<th scope="col">Slice</th>
			<td><span id="summary-sliceID"></span> </td>
		</tr>
		<tr class="summary-grant-or_budget-billing-row">
			<th scope="col">Authoriser</th>
			<td><span id="summary-authoriser"></span> </td>
		</tr>
		<tr class="summary-grant-or_budget-billing-row">
			<th scope="col">School/Unit</th>
			<td><span id="summary-schoolOrUnit"></span> </td>
		</tr>
		<tr class="summary-grant-or_budget-billing-row">
			<th scope="col">Subunit</th>
			<td><span id="summary-subunit"></span> </td>
		</tr>
		<tr class="summary-grant-or_budget-billing-row">
			<th scope="col">ProjectTitle</th>
			<td><span id="summary-projectTitle"></span> </td>
		</tr>
		<tr>
			<th scope="col">
				<h3 class="fs-title">Vault Access</h3></th>
			<td></td>
		</tr>
		<tr>
			<th scope="col">Owner UUN:</th>
			<td><span id="summary-vaultOwner"></span> </td>
		</tr>
		<tr>
			<th scope="col">NDMs</th>
			<td><span id="summary-nominatedDataManagers"></span> </td>
		</tr>
		<tr>
			<th scope="col">Depositors</th>
			<td><span id="summary-depositors"></span> </td>
		</tr>
		<tr>
			<th scope="col">
				<h3 class="fs-title">Pure Information</h3></th>
			<td></td>
		</tr>
		<tr>
			<th scope="col">Contact Person</th>
			<td><span id="summary-contactPerson"></span> </td>
		</tr>
		<tr>
			<th scope="col">Data Creators</th>
			<td><span id="summary-dataCreators"></span> </td>
		</tr>
	</tbody>
</table>

        </div>
        <div>
            Please make sure all information above is correct and click on confirm to process to your vault creation.
            Once a member of our staff validates your request you'll receive a confirmation email and be good to go!
        </div>
        <div class="alert alert-warning">
			<span class="glyphicon glyphicon glyphicon-alert" aria-hidden="true"></span><strong>PLEASE NOTE – this information will be public, and will be linked to the PI’s Pure profile.</strong>
            <div class="checkbox">
                <label>
                    <@spring.bind "vault.pureLink" />
                    <input type="hidden" name="_${spring.status.expression}" value="false"/>
                    <input id="pureLink-check" type="checkbox" name="${spring.status.expression}"
                           <#if spring.status.value?? && spring.status.value?string=="true">checked="true"</#if> /> I agree
                </label>
            </div>
        </div>
    </div>
    <button type="button" name="previous" class="previous action-button-previous btn btn-default" >&laquo; Previous</button>
    <#if vault.confirmed?c=="false">
    <button type="submit" name="save" value="Save" class="save action-button-previous btn btn-default" >
        <span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span> Save
    </button>
    <button type="submit" id="confirm" name="confirm" value="Confirm" class="action-button btn btn-success" disabled>
        <span class="glyphicon glyphicon-ok" aria-hidden="true"></span> Confirm
    </button>
    </#if>
</fieldset>
