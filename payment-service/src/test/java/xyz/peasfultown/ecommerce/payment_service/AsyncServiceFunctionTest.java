package xyz.peasfultown.ecommerce.payment_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import xyz.peasfultown.ecommerce.payment_service.config.RabbitMqConstants;
import xyz.peasfultown.ecommerce.payment_service.dto.CardToken;
import xyz.peasfultown.ecommerce.payment_service.dto.OrderCancellationMessage;
import xyz.peasfultown.ecommerce.payment_service.dto.OrderConfirmationMessage;
import xyz.peasfultown.ecommerce.payment_service.dto.PaymentConfirmationMessage;
import xyz.peasfultown.ecommerce.payment_service.entity.PaymentEntity;
import xyz.peasfultown.ecommerce.payment_service.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@ActiveProfiles({ "test", "rabbitmq" })
@EnableWireMock(
		@ConfigureWireMock(
				name = "user-service",
				baseUrlProperties = "USER_SERVICE_URL",
				portProperties = "user-service.port"
		)
)
@SpringBootTest
class AsyncServiceFunctionTest {
	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private RabbitAdmin rabbitAdmin;

	@Autowired
	private ObjectMapper oMapper;

	@Autowired
	private PaymentRepository paymentRepo;

	@InjectWireMock("user-service")
	private WireMockServer wireMockServer;

	void stub_getCardToken_returns200_returnsDecliningCard() throws Exception {
		CardToken cardToken = CardToken.builder()
				.cardId(UUID.randomUUID().toString())
				.expiryMonth(12)
				.expiryYear(2030)
				.lastFourDigits("0002")
				.token("tok_visa_0002_122030_abcd1234")
		.build();
		wireMockServer.stubFor(WireMock.get(urlPathTemplate("/api/v1/users/cards/{cardId}/token"))
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader("Content-Type", "application/json")
						.withBody(oMapper.writeValueAsString(cardToken))
						));
	}

	void stub_getCardToken_returns200_returnsGoodCard() throws Exception {
		CardToken cardToken = CardToken.builder()
				.cardId(UUID.randomUUID().toString())
				.expiryMonth(12)
				.expiryYear(2030)
				.lastFourDigits("4242")
				.token("tok_visa_4242_122030_abcd1234")
				.build();
		wireMockServer.stubFor(WireMock.get(urlPathTemplate("/api/v1/users/cards/{cardId}/token"))
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader("Content-Type", "application/json")
						.withBody(oMapper.writeValueAsString(cardToken))
				));
	}

	void stub_getCardToken_returns200_returnsExpiredCard() throws Exception {
		CardToken cardToken = CardToken.builder()
				.cardId(UUID.randomUUID().toString())
				.expiryMonth(12)
				.expiryYear(2024)
				.lastFourDigits("4242")
				.token("tok_visa_4242_122024_abcd1234")
				.build();
		wireMockServer.stubFor(WireMock.get(urlPathTemplate("/api/v1/users/cards/{cardId}/token"))
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader("Content-Type", "application/json")
						.withBody(oMapper.writeValueAsString(cardToken))
				));
	}

	void stub_getCardToken_returns404() throws Exception {
		wireMockServer.stubFor(WireMock.get(urlPathTemplate("/api/v1/users/cards/{cardId}/token"))
				.willReturn(aResponse()
						.withStatus(HttpStatus.NOT_FOUND.value())));
	}

	private PaymentConfirmationMessage buildPaymentConfirmationMessage() throws Exception {
		return PaymentConfirmationMessage.builder()
				.userId(UUID.randomUUID().toString())
				.cardId(UUID.randomUUID().toString())
				.orderId(UUID.randomUUID().toString())
				.amount(BigDecimal.valueOf(111.11))
				.build();

	}

	private Message buildRabbitMessage(PaymentConfirmationMessage messageToHandle) throws Exception {
		return MessageBuilder.withBody(oMapper.writeValueAsBytes(messageToHandle))
				.setHeader("__TypeId__", PaymentConfirmationMessage.class.getSimpleName())
				.setContentType(MessageProperties.CONTENT_TYPE_JSON)
				.build();
	}

	@BeforeEach
	void setup() {
		wireMockServer.resetAll();
		paymentRepo.deleteAll();
		rabbitAdmin.purgeQueue(RabbitMqConstants.order_cancelOrder_queue);
		rabbitAdmin.purgeQueue(RabbitMqConstants.order_confirmOrder_queue);
		rabbitAdmin.purgeQueue(RabbitMqConstants.payment_confirmPayment_queue);
	}

	@Test
	void handlePaymentConfirmationMessage_successfulPayment_savesInDatabase() throws Exception {
		PaymentConfirmationMessage messageToHandle = buildPaymentConfirmationMessage();
		Message testMessage = buildRabbitMessage(messageToHandle);

		stub_getCardToken_returns200_returnsGoodCard();
		rabbitTemplate.send(RabbitMqConstants.cart_checkout_payment_confirmPayment_routingKey, testMessage);

		await().atMost(10, TimeUnit.SECONDS)
				.pollInterval(500, TimeUnit.MILLISECONDS)
				.pollDelay(2, TimeUnit.SECONDS)
				.until(() -> {
					return paymentRepo.count() > 0;
				});

		PaymentEntity pe = paymentRepo.findAll().get(0);
		assertEquals(messageToHandle.getUserId(), pe.getUserId().toString());
		assertEquals(messageToHandle.getOrderId(), pe.getOrderId().toString());
		assertEquals(messageToHandle.getAmount(), pe.getAmount());
		assertEquals("tok_visa_4242_122030_abcd1234", pe.getCardToken());
		assertNotNull(pe.getTransactionId());
		assertEquals(PaymentEntity.PaymentStatus.SUCCESS, pe.getPaymentStatus());
	}

	@Test
	void handlePaymentConfirmationMessage_successfulPayment_sendsOrderConfirmationMessage() throws Exception {
		PaymentConfirmationMessage messageToHandle = buildPaymentConfirmationMessage();
		Message testMessage = buildRabbitMessage(messageToHandle);

		stub_getCardToken_returns200_returnsGoodCard();
		rabbitTemplate.send(RabbitMqConstants.cart_checkout_payment_confirmPayment_routingKey, testMessage);

		await().atMost(10, TimeUnit.SECONDS)
				.pollInterval(500, TimeUnit.MILLISECONDS)
				.pollDelay(2, TimeUnit.SECONDS)
				.until(() -> {
					return paymentRepo.count() > 0;
				});

		OrderConfirmationMessage sentMessage = rabbitTemplate.receiveAndConvert(RabbitMqConstants.order_confirmOrder_queue,
			10_000,
			new ParameterizedTypeReference<OrderConfirmationMessage>() {});
		assertEquals(messageToHandle.getOrderId(), sentMessage.getOrderId());
		assertNotNull(sentMessage.getPaymentId());
	}

	@Test
	void handlePaymentConfirmationMessage_failedPayment_savesInDb() throws Exception {
		PaymentConfirmationMessage messageToHandle = buildPaymentConfirmationMessage();
		Message testMessage = buildRabbitMessage(messageToHandle);

		stub_getCardToken_returns200_returnsDecliningCard();
		rabbitTemplate.send(RabbitMqConstants.cart_checkout_payment_confirmPayment_routingKey, testMessage);

		await().atMost(10, TimeUnit.SECONDS)
				.pollInterval(500, TimeUnit.MILLISECONDS)
				.pollDelay(2, TimeUnit.SECONDS)
				.until(() -> {
					return paymentRepo.count() > 0;
				});

		PaymentEntity pe = paymentRepo.findAll().get(0);
		assertEquals(messageToHandle.getUserId(), pe.getUserId().toString());
		assertEquals(messageToHandle.getOrderId(), pe.getOrderId().toString());
		assertEquals(messageToHandle.getAmount(), pe.getAmount());
		assertEquals("tok_visa_0002_122030_abcd1234", pe.getCardToken());
		assertNotNull(pe.getTransactionId());
		assertEquals(PaymentEntity.PaymentStatus.FAILED, pe.getPaymentStatus());
		assertEquals("Card declined", pe.getNote());
	}

	@Test
	void handlePaymentConfirmationMessage_cardDeclines_sendsOrderCancellationMessage() throws Exception {
		PaymentConfirmationMessage messageToHandle = buildPaymentConfirmationMessage();
		Message testMessage = buildRabbitMessage(messageToHandle);

		stub_getCardToken_returns200_returnsDecliningCard();
		rabbitTemplate.send(RabbitMqConstants.cart_checkout_payment_confirmPayment_routingKey, testMessage);

		await().atMost(10, TimeUnit.SECONDS)
				.pollInterval(500, TimeUnit.MILLISECONDS)
				.pollDelay(2, TimeUnit.SECONDS)
				.until(() -> {
					return paymentRepo.count() > 0;
				});

		OrderCancellationMessage sentMessage = rabbitTemplate.receiveAndConvert(RabbitMqConstants.order_cancelOrder_queue,
			10_000,
			new ParameterizedTypeReference<OrderCancellationMessage>() {});
		assertEquals(messageToHandle.getOrderId(), sentMessage.getOrderId());
		assertNotNull(sentMessage.getPaymentId());
	}

	@Test
	void handlePaymentConfirmationMessage_expiredCard_savesInDb() throws Exception {
		PaymentConfirmationMessage messageToHandle = buildPaymentConfirmationMessage();
		Message testMessage = buildRabbitMessage(messageToHandle);

		stub_getCardToken_returns200_returnsExpiredCard();
		rabbitTemplate.send(RabbitMqConstants.cart_checkout_payment_confirmPayment_routingKey, testMessage);

		await().atMost(10, TimeUnit.SECONDS)
				.pollInterval(500, TimeUnit.MILLISECONDS)
				.pollDelay(2, TimeUnit.SECONDS)
				.until(() -> {
					return paymentRepo.count() > 0;
				});

		PaymentEntity pe = paymentRepo.findAll().get(0);
		assertEquals(messageToHandle.getUserId(), pe.getUserId().toString());
		assertEquals(messageToHandle.getOrderId(), pe.getOrderId().toString());
		assertEquals(messageToHandle.getAmount(), pe.getAmount());
		assertEquals("tok_visa_4242_122024_abcd1234", pe.getCardToken());
		assertNotNull(pe.getTransactionId());
		assertEquals(PaymentEntity.PaymentStatus.FAILED, pe.getPaymentStatus());
		assertEquals("Card is expired", pe.getNote());
	}

	@Test
	void handlePaymentConfirmationMessage_whenUserServiceReturns404_failsPayment() throws Exception {
		PaymentConfirmationMessage messageToHandle = buildPaymentConfirmationMessage();
		Message testMessage = buildRabbitMessage(messageToHandle);
		stub_getCardToken_returns404();

		rabbitTemplate.send(RabbitMqConstants.cart_checkout_payment_confirmPayment_routingKey, testMessage);

		await().atMost(10, TimeUnit.SECONDS)
				.pollDelay(2, TimeUnit.SECONDS)
				.pollInterval(500, TimeUnit.MILLISECONDS)
				.until(() -> {
					return paymentRepo.count() > 0;
				});

		PaymentEntity pe = paymentRepo.findAll().get(0);
		assertEquals(messageToHandle.getUserId(), pe.getUserId().toString());
		assertEquals(messageToHandle.getOrderId(), pe.getOrderId().toString());
		assertEquals(messageToHandle.getAmount(), pe.getAmount());
		assertNull(pe.getCardToken());
		assertNull(pe.getTransactionId());
		assertEquals(PaymentEntity.PaymentStatus.FAILED, pe.getPaymentStatus());
		assertEquals("Card token not found", pe.getNote());
	}

	@Test
	void handlePaymentConfirmationMessage_whenUserServiceReturns404_sendsOrderCancellationMessage() throws Exception {
		PaymentConfirmationMessage messageToHandle = buildPaymentConfirmationMessage();
		Message testMessage = buildRabbitMessage(messageToHandle);
		stub_getCardToken_returns404();

		rabbitTemplate.send(RabbitMqConstants.cart_checkout_payment_confirmPayment_routingKey, testMessage);

		await().atMost(10, TimeUnit.SECONDS)
				.pollDelay(2, TimeUnit.SECONDS)
				.pollInterval(500, TimeUnit.MILLISECONDS)
				.until(() -> {
					return paymentRepo.count() > 0;
				});

		OrderCancellationMessage sentMessage = rabbitTemplate.receiveAndConvert(RabbitMqConstants.order_cancelOrder_queue,
				10_000,
				new ParameterizedTypeReference<OrderCancellationMessage>() {});
		assertEquals(messageToHandle.getOrderId(), sentMessage.getOrderId());
		assertNotNull(sentMessage.getPaymentId());
	}
}
