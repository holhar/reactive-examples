package de.holhar.spring.reactiveexamples;

import de.holhar.spring.reactiveexamples.domain.Person;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PersonInterface {

    Mono<Person> getById(Integer id);

    Flux<Person> findAll();
}
