package io.github.rabbitmq.flow;

import reactor.core.Disposable;

public interface Flow {

    Disposable start();

}
