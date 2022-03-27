package com.payments.service;


import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The {@link PaymentService}
 * provides interaction
 * with payment methods
 * ECommPay
 *
 * @author Olga Savin
 */
@Service
public class PaymentService {

    @Value("${server.url.form}")
    String formUrl;

    @Value("${server.url.recurring}")
    String recurringUrl;

    @Value("${projectId}")
    int projectId;

    @Value("${secretKey}")
    String secretKey;

    @Value("${server.charset}")
    String charset;


    /**
     * Method for URL encoding payment params
     *
     * @param customerId is a unique number used to identify your customer
     * @param amount     amount in cents
     * @param currency   ISO 4217 code USD EUR
     * @return URL encoded param
     */
    public String getFormUrl(String customerId, int amount, String currency) {
        Map<String, Object> payment = new HashMap<>();
        payment.put("project_id", projectId);
        payment.put("payment_currency", currency);
        payment.put("payment_amount", amount);
        payment.put("customer_id", customerId);
        payment.put("payment_id", UUID.randomUUID().toString());

        String signature = "&signature=".concat(encode(sign(payment, secretKey)));
        String query = payment.entrySet().stream().map(e -> e.getKey() + "=" + encode(e.getValue())).collect(Collectors.joining("&"));

        return formUrl.concat("?").concat(query).concat(signature);

    }

    /**
     * Method for URL encoding payment params
     *
     * @param param payment param value
     * @return URL encoded param
     */
    private String encode(Object param) {
        try {
            return URLEncoder.encode(param.toString(), charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method for make PaymentMap
     *
     * @param customerId is a unique number used to identify your customer
     * @param amount     amount in cents
     * @param currency   ISO 4217 code USD EUR
     * @return signature
     */
    public Map<String, Object> createPaymentMap(int amount, String currency, String ip, String customerId, int project_id, int recurringId, String paymentId) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> recurring = new HashMap<>();
        recurring.put("id", recurringId);
        map.put("recurring", recurring);

        Map<String, Object> payment = new HashMap<>();
        payment.put("amount", amount);
        payment.put("currency", currency);
        map.put("payment", payment);

        Map<String, Object> customer = new HashMap<>();
        customer.put("ip_address", ip);
        customer.put("id", customerId);
        map.put("customer", customer);

        Map<String, Object> general = new HashMap<>();
        general.put("project_id", project_id);
        general.put("payment_id", paymentId);
        map.put("general", general);

        return map;
    }

    /**
     * Method for make recurring payment
     *
     * @param map is payment key value object
     */
    public void makePayment(Map<String, Object> map) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String signature = sign(map, secretKey);

        HashMap<String, Object> general = (HashMap<String, Object>) map.get("general");
        general.put("signature", signature);
        map.put("general", general);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(recurringUrl, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            String body = response.getBody();
            if (!isEmptyString(body)) {
                JSONObject jsonObject = new JSONObject(body);

                String status = null;
                if (jsonObject.has("status")) {
                    if (jsonObject.get("status") instanceof String) status = jsonObject.getString("status");
                }

                String requestId = null;
                if (jsonObject.has("request_id")) {
                    if (jsonObject.get("request_id") instanceof String) requestId = jsonObject.getString("request_id");
                }

                String paymentId = null;
                if (jsonObject.has("payment_id")) {
                    if (jsonObject.get("payment_id") instanceof String) paymentId = jsonObject.getString("payment_id");
                }

                System.out.println("status:" + status);
                System.out.println("requestId:" + requestId);
                System.out.println("paymentId:" + paymentId);
            }

        } else {
            System.out.println(response);
        }
    }

    boolean isEmptyString(String string) {
        return string == null || string.isEmpty();
    }

    /**
     * Method for make signature
     *
     * @param params parameters with which signature obtained
     * @return signature
     */
    public String sign(Map<String, Object> params, String secret) {

        String[] IGNORE_KEYS = new String[]{"frame_mode"};
        String ALGORITHM = "HmacSHA512";
        Map paramsToSing = getParamsToSing(params, "", IGNORE_KEYS);
        List<String> paramsListToSing = new ArrayList<String>(paramsToSing.values());
        Collections.sort(paramsListToSing);

        String paramsStringToSing = String.join(";", paramsListToSing);

        try {
            Mac shaHMAC = Mac.getInstance(ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), ALGORITHM);
            shaHMAC.init(secretKey);
            return Base64.getEncoder().encodeToString(shaHMAC.doFinal(paramsStringToSing.getBytes()));
        } catch (GeneralSecurityException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Method for preparing params
     *
     * @param params map with params
     * @param prefix add before key
     * @param ignore ignore specific keys
     * @return prepared map with params
     */
    private Map getParamsToSing(Map<String, Object> params, String prefix, String[] ignore) {

        Map<String, String> paramsToSign = new HashMap<>();
        ignore = ignore != null ? ignore : new String[]{};

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (Arrays.asList(ignore).contains(entry.getKey())) {
                continue;
            }

            String key = prefix + (prefix.equals("") ? "" : ':') + entry.getKey();
            Object valueObject = entry.getValue();

            if (valueObject instanceof Boolean) {
                valueObject = Boolean.parseBoolean(valueObject.toString()) ? '1' : '0';
            }

            if (valueObject instanceof List) {
                HashMap<String, Object> tempMap = new HashMap<String, Object>();

                for (int i = 0; i < ((List) valueObject).size(); i++) {
                    tempMap.put(String.valueOf(i), ((List) valueObject).get(i));
                }

                valueObject = tempMap;
            }

            if (valueObject instanceof Map) {
                paramsToSign.putAll(getParamsToSing((Map) valueObject, key, ignore));
            } else {
                paramsToSign.put(key, key + ':' + valueObject.toString());
            }
        }

        return paramsToSign;
    }
}