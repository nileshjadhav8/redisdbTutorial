package sg.edu.nus.iss.app.workshop14;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import sg.edu.nus.iss.app.workshop14.models.Contact;
import sg.edu.nus.iss.app.workshop14.service.ContactsRedis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ContactsRedisTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplateMock;

    @Mock
    private ListOperations<String, Object> listOpsMock;

    @Mock
    private HashOperations<String, Object, Object> hashOpsMock;

    @InjectMocks
    private ContactsRedis contactsRedis;

    @Test
    public void testSave() {
        Contact contact = new Contact("1", "John Doe", null, null);
        when(redisTemplateMock.opsForList()).thenReturn(listOpsMock);
        when(redisTemplateMock.opsForHash()).thenReturn(hashOpsMock);

        contactsRedis.save(contact);

        verify(listOpsMock, times(1)).leftPush(eq("contactlist"), eq(contact.getId()));
        verify(hashOpsMock, times(1)).put(eq("contactlist_Map"), eq(contact.getId()), eq(contact));
    }

    @Test
    public void testFindById() {
        Contact contact = new Contact("1", "John Doe", null, null);
        when(redisTemplateMock.opsForHash()).thenReturn(hashOpsMock);
        when(hashOpsMock.get(eq("contactlist_Map"), eq(contact.getId()))).thenReturn(contact);

        Contact result = contactsRedis.findById(contact.getId());

        verify(hashOpsMock, times(1)).get(eq("contactlist_Map"), eq(contact.getId()));
        assertEquals(contact, result);
    }

    @Test
    public void testFindAll() {
        List<Object> fromContactList = new ArrayList<>();
        Contact contact1 = new Contact("1", "John Doe", null, null);
        Contact contact2 = new Contact("2", "Jane Doe", null, null);
        fromContactList.add(contact1.getId());
        fromContactList.add(contact2.getId());
        when(redisTemplateMock.opsForList()).thenReturn(listOpsMock);
        when(redisTemplateMock.opsForHash()).thenReturn(hashOpsMock);
        when(listOpsMock.range(eq("contactlist"), eq(0L), eq(10L))).thenReturn(fromContactList);
        when(hashOpsMock.multiGet(eq("contactlist_Map"), eq(fromContactList))).thenReturn(List.of(contact1, contact2));

        List<Contact> result = contactsRedis.findAll(0);

        verify(listOpsMock, times(1)).range(eq("contactlist"), eq(0L), eq(10L));
        verify(hashOpsMock, times(1)).multiGet(eq("contactlist_Map"), eq(fromContactList));
        assertEquals(2, result.size());
        assertEquals(contact1, result.get(0));
        assertEquals(contact2, result.get(1));
    }
}

