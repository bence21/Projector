package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.BibleDTO;
import com.bence.projector.common.dto.BookDTO;
import com.bence.projector.server.api.assembler.BibleAssembler;
import com.bence.projector.server.backend.model.Bible;
import com.bence.projector.server.backend.model.Book;
import com.bence.projector.server.backend.service.BibleService;
import com.bence.projector.server.backend.service.StatisticsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {BibleResource.class})
@WebAppConfiguration
public class BibleResourceTest {

    @InjectMocks
    private BibleResource bibleResource;
    @MockBean
    private BibleService bibleService;
    @MockBean
    private BibleAssembler bibleAssembler;
    @MockBean
    private StatisticsService statisticsService;
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(bibleResource).build();
    }

    @Test
    public void testGetAllTitles() throws Exception {
        List<Bible> bibles = new ArrayList<>();
        Bible bible = getATestBible();
        bibles.add(bible);
        when(bibleService.findAll()).thenReturn(bibles);
        List<BibleDTO> biblesDTOS = new ArrayList<>();
        BibleDTO bibleDTO = getBibleDTO(bible);
        biblesDTOS.add(bibleDTO);
        when(bibleAssembler.createDtoList(bibles)).thenReturn(biblesDTOS);
        String urlTemplate = "/api/bibleTitles";
        mockMvc.perform(get(urlTemplate))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    public void testGetABible() throws Exception {
        Bible bible = getATestBible();
        String uuid = bible.getUuid();
        when(bibleService.findOneByUuid(uuid)).thenReturn(bible);
        when(bibleAssembler.createDto(bible)).thenReturn(getBibleDTO(bible));
        mockMvc.perform(get("/api/bible/{uuid}", uuid))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private BibleDTO getBibleDTO(Bible bible) {
        BibleDTO bibleDTO = new BibleDTO();
        bibleDTO.setName(bible.getName());
        bibleDTO.setShortName(bible.getShortName());
        bibleDTO.setBooks(getBookDTOS(bible.getBooks()));
        return bibleDTO;
    }

    private List<BookDTO> getBookDTOS(List<Book> books) {
        if (books == null) {
            return null;
        }
        ArrayList<BookDTO> bookDTOS = new ArrayList<>();
        for (Book book : books) {
            BookDTO bookDTO = getBookDTO(book);
            bookDTOS.add(bookDTO);
        }
        return bookDTOS;
    }

    private BookDTO getBookDTO(Book book) {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setTitle(book.getTitle());
        return bookDTO;
    }

    private Bible getATestBible() {
        Bible bible = new Bible();
        bible.setName("Test");
        bible.setCreatedDate(new Date());
        bible.setModifiedDate(bible.getCreatedDate());
        bible.setShortName("Tt");
        bible.setBooks(getBooks());
        return bible;
    }

    private List<Book> getBooks() {
        ArrayList<Book> books = new ArrayList<>();
        Book book = getBook();
        books.add(book);
        return books;
    }

    private Book getBook() {
        Book book = new Book();
        book.setShortName("T book");
        book.setTitle("Test book");
        return book;
    }
}