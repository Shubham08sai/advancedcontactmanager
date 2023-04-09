console.log("this is console file")
const toggleSidebar = () => {
	if ($(".sidebar").is(":visible")) {
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%")
	} else {
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%")
	}
};
const search = () => {
	//	console.log("searching....")

	let query = $("#search-input").val()
	if (query == '') {

		$(".search-result").hide();

	} else {
		//search
		console.log(query);
		//sending request to server
		let url = `http://localhost:8080/search/${query}`;

		fetch(url).then((response) => {
			return response.json();
		} ).then((data) => {
			//data.......... 
			console.log(data);
			let text = `<div class='list-group'>`;
			data.forEach((contact) => {
				text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-action'> ${contact.name}</a>`
			});
			text += `</div>`;
			$(".search-result").html(text);
			$(".search-result").show();
		});
	}
};

//first request to server to create order
const paymentStart = () => {
	console.log("payment started...");
	var amount = $("#payment_field").val();
	console.log(amount);
	if (amount == "" || amount == null) {
		//alert("amount is required !!!");
		swal("Alert!!!", "amount is required !!!!", "error");
		return;
	}

	//we will use ajax to send request to server for  create order- jquery
	$.ajax({
		url: "/user/create_order",
		data: JSON.stringify({ amount: amount, info: "order_request" }),
		contentType: "application/json",
		type: "POST",
		datatype: "json",
		success: function(response) {
			//invoked when success
			console.log(response);
			let response1 = JSON.parse(response);
			console.log(response1.status);
			if (response1.status == "created") {
				console.log('created');
				//open payment form
				let options = {

					key: "rzp_test_aY0jnnG05j4MrR", // Enter the Key ID generated from the Dashboard
					amount: response1.amount, // Amount is in currency subunits. Default currency is INR. Hence, 50000 refers to 50000 paise
					currency: "INR",
					name: "Advanced Contact Manager",
					description: "Test Transaction",
					image: "https://www.w3schools.com/css/paris.jpg",
					order_id: response1.id, //This is a sample Order ID. Pass the `id` obtained in the response of Step 1
					handler: function(response2) {
						alert(response2.razorpay_payment_id);
						alert(response2.razorpay_order_id);
						alert(response2.razorpay_signature);
						console.log("payment successfull !!");
						//						alert("congrates !! payment is successfull !!");

						updatePaymentOnServer
						(
                               response2.razorpay_payment_id,
								response2.razorpay_order_id,
								"paid"
						);


					},
					prefill: {
						name: "",
						email: "",
						contact: "",
					},
					notes: {
						address: "learn code with me",
					},
					theme: {
						color: "#3399cc",
					},
				};
				let rzp = new Razorpay(options);
				rzp.on("payment.failed", function(response3) {
					console.log(response3.error.code);
					console.log(response3.error.description);
					console.log(response3.error.source);
					console.log(response3.error.step);
					console.log(response3.error.reason);
					console.log(response3.error.metadata.order_id);
					console.log(response3.error.metadata.payment_id);
					alert("OOPs !! payment is Failed !!");

				});

				rzp.open();

			}
		},
		error: function(error) {
			//invoked when error
			console.log(error);
			alert("something went wrong !!!");
		},
	});
};
function updatePaymentOnServer(payment_id, order_id, status) 
{
	$.ajax({
		url: "/user/update_order",
		data: JSON.stringify({ payment_id: payment_id, order_id: order_id, status: status }),
		contentType: "application/json",
		type: "POST",
		datatype: "json",
		success: function(response4) {
			console.log(response4);
			swal("Good job!", "payment is successfull !!", "success");
		},
		error: function(error) {
			console.log(error);
			swal("failed!!", " payment is successfull  OOPs !! we did didn't get on server", "error");

		},
	});
}

