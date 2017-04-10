package org.bank.atm.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.bank.atm.Application;
import org.bank.atm.model.Account;
import org.bank.atm.service.AccountRepository;
import org.bank.atm.util.ATMUtil;
import org.bank.atm.util.Banknote;
import org.bank.atm.util.BanknoteComparator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest({"server.port=0", "spring.datasource.url=jdbc:postgresql://localhost:5432/modulr_test"})
@WebIntegrationTest
public class ATMControllerITest {

    private static final Logger log = Logger.getLogger(ATMControllerITest.class.getName());

    @Value("${local.server.port}")
    private int port;

    @Autowired
    AccountRepository repository;

    @Autowired
    ATMUtil atmUtil;

    private RestTemplate restTemplate = new TestRestTemplate();

    private String getBaseUrl() {
        return "http://localhost:" + port + "atm/";
    }

    private Account account1;
    private Account account2;
    private Account account3;

    @Before
    public void init() {
        account1 = new Account("01001", 2738.59);
        account2 = new Account("01002", 23.00);
        account3 = new Account("01003", 0.00);
        repository.save(account1);
        repository.save(account2);
        repository.save(account3);

        log.info("Integration test initialised");
    }

    @After
    public void clean() {
        repository.deleteAll();

        log.info("Cleanup finished");
    }

    @Test
    public void testATMController() {
        log.info("Integration test started");

        String urlReplenish = getBaseUrl() + "replenish?five=10&ten=50&twenty=90&fifty=8";

        ResponseEntity<String> responseReplenish = restTemplate.getForEntity(urlReplenish, String.class);

        HttpStatus statusReplenish = responseReplenish.getStatusCode();
        assertThat(HttpStatus.OK, is(equalTo(statusReplenish)));

        Map<Banknote, Long> banknoteCountMap = atmUtil.getBanknoteCount();
        assertThat(10L, is(equalTo(banknoteCountMap.get(Banknote.FIVE))));
        assertThat(50L, is(equalTo(banknoteCountMap.get(Banknote.TEN))));
        assertThat(90L, is(equalTo(banknoteCountMap.get(Banknote.TWNETY))));
        assertThat(8L, is(equalTo(banknoteCountMap.get(Banknote.FIFTY))));

        String urlCheckBalance = getBaseUrl() + "checkBalance?accountNumber=01001";

        ResponseEntity<String> responseCheckBalance = restTemplate.getForEntity(urlCheckBalance, String.class);

        HttpStatus statusCheckBalance = responseCheckBalance.getStatusCode();
        String resultAccount = responseCheckBalance.getBody();

        assertThat(HttpStatus.OK, is(equalTo(statusCheckBalance)));
        assertThat(account1.toString(), is(equalTo(resultAccount)));

        String urlWithdraw = getBaseUrl() + "withdraw?accountNumber=01001&amount=50";

        ResponseEntity<String> responseWithdraw = restTemplate.getForEntity(urlWithdraw, String.class);

        HttpStatus statusWithdraw = responseWithdraw.getStatusCode();
        String resultBanknotes = responseWithdraw.getBody();

        Map<Banknote, Long> expectedBanknotes = new TreeMap<>(new BanknoteComparator());
        expectedBanknotes.put(Banknote.FIVE, 2L);
        expectedBanknotes.put(Banknote.TWNETY, 2L);

        assertThat(HttpStatus.OK, is(equalTo(statusWithdraw)));
        assertThat(expectedBanknotes.toString(), is(equalTo(resultBanknotes)));

        log.info("Integration test finished");
    }
}
