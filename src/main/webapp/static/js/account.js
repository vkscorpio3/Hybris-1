$(document).ready(function() {

	function openEditPopUp(address) {
		var addressId = address.split('_')[1];
		var ajaxData = {};
		ajaxData['addressId'] = addressId;
		$.ajax({
			type: 'GET',
			url: '/address/getEditAddress',
			dataType: 'json',
			data: $.param(ajaxData),
			success: function(res) {
				var form = $('#modal_addrForm');
				populateAddrForm(form, res);
			}
		});
	}

	function populateAddrForm(form, addrObj) {
		form.find('#modal_addrCompany').val(addrObj.companyName);
		form.find('#modal_addrCity').val(addrObj.city);
		form.find('#modal_addrPostalCode').val(addrObj.postalCode);
		form.find('#modal_addrAddress').val(addrObj.address);
		form.find('#modal_addrCountry').val(addrObj.country);
		form.find('#modal_addressId').val(addrObj.addressId);
	}

	function collectAddrForm(form, ajaxData) {
		ajaxData['companyName'] = form.find('#modal_addrCompany').val();
		ajaxData['city'] = form.find('#modal_addrCity').val();
		ajaxData['postalCode'] = form.find('#modal_addrPostalCode').val();
		ajaxData['address'] = form.find('#modal_addrAddress').val();
		ajaxData['country'] = form.find('#modal_addrCountry').val();
		ajaxData['addressId'] = form.find('#modal_addressId').val();
	}

	$('button[name=delete_address]').bind('click', function() {
		var ajaxData = {};
		ajaxData['addressId'] = this.id.split('_')[1];
		$.ajax({
			type: 'POST',
			url: '/address/deleteAddress',
			dataType: 'json',
			data: $.param(ajaxData),
			success: function(res) {
				if (res.success) {
					$('#deleteAddress_' + res.deletedId).parent().parent().remove();
				} else {
					console.log(res.err);
				}
			}
		});
	});

	$('button[name=edit_address]').bind('click', function() {
		openEditPopUp(this.id);
	});

	$('#add_address').bind('click', function() {
		var form = $('#modal_addrForm')[0];
		form.reset();
		$('#modal_addressId').val('');
	});

	$('#modal_addrForm').submit(function(event) {
		event.preventDefault();
		var ajaxData = {};
		collectAddrForm($('#modal_addrForm'), ajaxData);
		$.ajax({
			type: 'POST',
			url: '/address/modifyAddress',
			dataType: 'json',
			data: $.param(ajaxData),
			success: function(res) {
				if (res.success) {
					$('#account_addresses').find('tbody')
						.append($('<tr>')
							.append($('<td>')
								.append(ajaxData['companyName']))
							.append($('<td>')
								.append(ajaxData['country']))
							.append($('<td>')
								.append(ajaxData['city']))
							.append($('<td>')
								.append(ajaxData['postalCode']))
							.append($('<td>')
								.append($('<button>')
									.attr('type', 'button')
									.attr('id', 'editAddress_')
									.attr('class', 'btn btn-primary')
									.attr('name', 'edit_address' + res.newAddressId)
									.attr('data-toggle', 'modal')
									.attr('data-target', '#addressModal')
									.text('Edit'))
								.append('&nbsp;')
								.append($('<button>')
									.attr('type', 'button')
									.attr('id', 'deleteAddress_' + res.newAddressId)
									.attr('class', 'btn btn-danger')
									.attr('name', 'delete_address')
									.text('Delete')))
					);
				} else {
					console.log(res.err);
				}
			}
		});
		$('#addressModal').modal('hide');
	});
});