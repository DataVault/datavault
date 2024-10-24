<fieldset id="summary-fieldset">
	<div class="form-card">
		<h2 class="fs-title">Summary</h2>
		<div>
			<table class="table table-sm">
				<tbody>
					<tr>
						<th scope="col">
							<h3 class="fs-title">Vault Information</h3>
						</th>
						<td></td>
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
							<h3 class="fs-title">Billing</h3>
						</th>
						<td></td>
					</tr>
					<tr>
						<th scope="col">Billing Type</th>
						<td><span id="summary-billing-type"></span> </td>
					</tr>
					<tr class="summary-slice-billing-row">
						<th scope="col">Slice</th>
						<td><span id="summary-sliceID"></span> </td>
					</tr>
					<tr class="summary-budget-authoriser-billing-row">
						<th scope="col">Authoriser</th>
						<td><span id="summary-budget-authoriser"></span> </td>
					</tr>
					<tr class="summary-school-or-unit-billing-row">
						<th scope="col">School/Unit</th>
						<td><span id="summary-school-or-unit"></span> </td>
					</tr>
					<tr class="summary-subunit-billing-row">
						<th scope="col">Subunit</th>
						<td><span id="summary-subunit"></span> </td>
					</tr>
					<tr class="summary-project-title-billing-row">
						<th scope="col">Project Title</th>
						<td><span id="summary-project-title"></span> </td>
					</tr>
					<tr class="summary-billing-grant-end-date-billing-row">
						<th scope="col">Billing Grant End Date</th>
						<td><span id="summary-billing-grant-end-date"></span> </td>
					</tr>
					<tr class="summary-payment-details-billing-row">
						<th scope="col">Payment Details</th>
						<td><textarea id="summary-payment-details" type="text"
								readonly
								rows="4" cols="60">
							</textarea></td>
					</tr>
					<tr>
					<tr>
						<th scope="col">
							<h3 class="fs-title">Vault Access</h3>
						</th>
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
							<h3 class="fs-title">Pure Information</h3>
						</th>
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
	</div>
	<a name="admin-pending-vault-summary" class="btn btn-primary" style="margin-bottom: 0;"
		href="${springMacroRequestContext.getContextPath()}/admin/pendingVaults/summary/${vaultID}">
		<< Return Admin Pending Vault Summary</a>
			<button type="submit" name="save" value="Save" class="save action-button-previous btn btn-success">
				<span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span> Save
			</button>
			<button type="button" name="next" class="next action-button btn btn-default">Next Step &raquo;</button>
</fieldset>