package com.course.bff.books.controlles;

import com.course.bff.books.models.Book;
import com.course.bff.books.requests.CreateBookCommand;
import com.course.bff.books.responses.BookResponse;
import com.course.bff.books.services.BookService;
import com.course.bff.books.services.RedisService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/books")
public class BookController {

    private final static Logger logger = LoggerFactory.getLogger(BookController.class);
    private final BookService bookService;
    private final RedisService redisService;
    private final MeterRegistry meterRegistry;
    private Counter requestCounter;
    private Counter errorCounter;
    private Timer executionTimer;

    public BookController(BookService bookService, RedisService redisService, MeterRegistry registry) {
        this.bookService = bookService;
        this.redisService = redisService;
        this.meterRegistry = registry;
        initMetrics();
    }


    @GetMapping()
    public Collection<BookResponse> getBooks() throws Exception {
        return executionTimer.recordCallable(() -> {
            logger.info("Get book list");
            List<BookResponse> bookResponses = new ArrayList<>();

            this.bookService.getBooks().forEach(book -> {
                BookResponse bookResponse = createBookResponse(book);
                bookResponses.add(bookResponse);
            });
            requestCounter.increment();
            return bookResponses;
        });
    }

    @GetMapping("/{id}")
    public BookResponse getById(@PathVariable UUID id) {
        logger.info(String.format("Find book by id %s", id));
        Optional<Book> bookSearch = this.bookService.findById(id);
        if (bookSearch.isEmpty()) {
            errorCounter.increment();
            throw new RuntimeException("Book isn't found");
        }

        requestCounter.increment();
        return createBookResponse(bookSearch.get());
    }

    @PostMapping()
    public BookResponse createBooks(@RequestBody CreateBookCommand createBookCommand) {
        logger.info("Create books");
        Book book = this.bookService.create(createBookCommand);
        BookResponse authorResponse = createBookResponse(book);
        redisService.sendPushNotification(authorResponse);
        requestCounter.increment();
        return authorResponse;
    }

    private BookResponse createBookResponse(Book book) {
        BookResponse bookResponse = new BookResponse();
        bookResponse.setId(book.getId());
        bookResponse.setAuthorId(book.getAuthorId());
        bookResponse.setPages(book.getPages());
        bookResponse.setTitle(book.getTitle());
        return bookResponse;
    }

    private void initMetrics() {
        requestCounter = meterRegistry.counter("request_count",
                "ControllerName", this.getClass().getSimpleName(),
                "ServiceName", bookService.getClass().getSimpleName());
        errorCounter = meterRegistry.counter("error_count",
                "ControllerName", this.getClass().getSimpleName(),
                "ServiceName", bookService.getClass().getSimpleName());
        executionTimer = meterRegistry.timer("execution_duration",
                "ControllerName", this.getClass().getSimpleName(),
                "ServiceName", bookService.getClass().getSimpleName());
    }
}
