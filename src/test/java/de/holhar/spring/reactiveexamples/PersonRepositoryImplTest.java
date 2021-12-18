package de.holhar.spring.reactiveexamples;

import de.holhar.spring.reactiveexamples.domain.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersonRepositoryImplTest {

    private static final Logger logger = LoggerFactory.getLogger(PersonRepositoryImplTest.class);

    PersonRepositoryImpl personRepository;

    @BeforeEach
    void setUp() {
        personRepository = new PersonRepositoryImpl();
    }

    @Test
    void getByIdBlock() {
        Mono<Person> personMono = personRepository.getById(4);

        Person person = personMono.block();

        StepVerifier.create(personMono).expectNextCount(1).verifyComplete();
        logger.info("{}", person);
        assertNotNull(person);
        assertEquals("Jesse", person.getFirstName());
        assertEquals("Porter", person.getLastName());
    }

    @Test
    void getByIdBlockNullResult() {
        Mono<Person> personMono = personRepository.getById(5);

        StepVerifier.create(personMono).expectNextCount(0).verifyComplete();
        Person person = personMono.block();
        logger.info("{}", person);
        assertNull(person);
    }

    @Test
    void getByIdSubscribe() {
        Mono<Person> personMono = personRepository.getById(4);

        StepVerifier.create(personMono).expectNextCount(1).verifyComplete();
        personMono.subscribe(person -> {
            logger.info("{}", person);
            assertNotNull(person);
            assertEquals("Jesse", person.getFirstName());
            assertEquals("Porter", person.getLastName());
        });
    }

    @Test
    void getByIdMapFunction() {
        Mono<Person> personMono = personRepository.getById(4);

        StepVerifier.create(personMono).expectNextCount(1).verifyComplete();
        personMono.map(Person::getFirstName)
                .subscribe(firstName -> {
                    logger.info("from map: '{}'", firstName);
                    assertEquals("Jesse", firstName);
                });
    }

    @Test
    void fluxBlockFirst() {
        Flux<Person> personFlux = personRepository.findAll();

        StepVerifier.create(personFlux).expectNextCount(4).verifyComplete();
        // Only emits first person object from the flux
        Person person = personFlux.blockFirst();
        logger.info("{}", person);
    }

    @Test
    void fluxSubscribe() {
        Flux<Person> personFlux = personRepository.findAll();

        StepVerifier.create(personFlux).expectNextCount(4).verifyComplete();
        // Emitting all person objects from the flux
        personFlux.subscribe(person -> logger.info("{}", person));
    }

    @Test
    void fluxToListMono() {
        Flux<Person> personFlux = personRepository.findAll();

        Mono<List<Person>> personListMono = personFlux.collectList();

        personListMono.subscribe(list -> list.forEach(person -> logger.info("{}", person)));
    }

    @Test
    void fluxFindPersonById() {
        Flux<Person> personFlux = personRepository.findAll();

        final Integer id = 3;

        StepVerifier.create(personFlux).expectNextCount(4).verifyComplete();
        Mono<Person> personMono = personFlux.filter(person -> person.getId().equals(id)).next();
        personMono.subscribe(person -> logger.info("{}", person));
    }

    @Test
    void fluxFindPersonByIdNotFound() {
        Flux<Person> personFlux = personRepository.findAll();

        final Integer id = 8;

        Mono<Person> personMono = personFlux.filter(person -> person.getId().equals(id)).next();

        // Fails silently; nothing will be logged, the personMono is empty
        personMono.subscribe(person -> logger.info("{}", person));
    }

    @Test
    void fluxFindPersonByIdNotFoundWithException() {
        Flux<Person> personFlux = personRepository.findAll();

        final Integer id = 8;

        Mono<Person> personMono = personFlux.filter(person -> person.getId().equals(id)).single();

        personMono.doOnError(throwable -> logger.info("I went boom"))
                .onErrorReturn(Person.builder().id(id).build())
                .subscribe(person -> logger.info("{}", person));
    }
}