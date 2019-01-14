package io.github.rabbitmq.flow;

import reactor.rabbitmq.OutboundMessageResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PublishConfirmResult {

    private int numberOfEventsSent;
    private List<OutboundMessageResult> ackedResults;
    private List<OutboundMessageResult> nackedResults;

    public PublishConfirmResult(int numberOfEventsSent, List<OutboundMessageResult> results) {
        this.numberOfEventsSent = numberOfEventsSent;
        this.ackedResults = results.stream().filter(OutboundMessageResult::isAck).collect(Collectors.toList());
        this.nackedResults = results.stream().filter(result -> !result.isAck()).collect(Collectors.toList());
    }

    public static PublishConfirmResult FALLBACK_VALUE = new PublishConfirmResult(-1, new ArrayList<>());

    public boolean allSent() {
        return ackedResults.size() == numberOfEventsSent;
    }

}
