package com.hubspot.imap;

import com.google.common.collect.Sets;
import com.hubspot.imap.client.ImapClient;
import com.hubspot.imap.imap.command.fetch.items.FetchDataItem.FetchDataItemType;
import com.hubspot.imap.imap.message.ImapMessage;
import com.hubspot.imap.imap.response.ResponseCode;
import com.hubspot.imap.imap.response.tagged.FetchResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

public class FetchEventListenerTest {

  ImapClient client;

  @Before
  public void getClient() throws Exception {
    client = TestUtils.getLoggedInClient();
  }

  @After
  public void closeClient() throws Exception {
    client.close();
  }

  @Test
  public void testOnFetch_doesFireFetchEvent() throws Exception {
    Set<ImapMessage> eventMessages = Sets.newHashSet();
    CountDownLatch countDownLatch = new CountDownLatch(1);
    client.getState().addFetchEventListener((event) -> {
      eventMessages.addAll(event.getMessages());
      countDownLatch.countDown();
    });

    client.open("[Gmail]/All Mail", true).sync();
    FetchResponse response = client.fetch(1, Optional.<Long>empty(), FetchDataItemType.UID).get();

    countDownLatch.await();

    assertThat(response.getCode()).isEqualTo(ResponseCode.OK);
    assertThat(eventMessages.size()).isGreaterThan(0);
    assertThat(eventMessages).containsAll(response.getMessages());
  }
}