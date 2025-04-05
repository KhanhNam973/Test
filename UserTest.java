package cinema.ticket.booking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import cinema.ticket.booking.exception.MyBadRequestException;
import cinema.ticket.booking.model.Account;
import cinema.ticket.booking.model.Role;
import cinema.ticket.booking.model.enumModel.ERole;
import cinema.ticket.booking.model.enumModel.UserStatus;
import cinema.ticket.booking.repository.RoleRepository;
import cinema.ticket.booking.repository.UserRepository;
import cinema.ticket.booking.response.AccountSummaryResponse;
import cinema.ticket.booking.service.impl.UserServiceImpl;
import jakarta.transaction.Transactional;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@Transactional
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Mock
    private UserRepository userRepositoryMock;

    @Autowired
    private RoleRepository roleRepository;

    @Mock
    private RoleRepository roleRepositoryMock;

    @Autowired
    private UserServiceImpl userServiceImpl;

    @InjectMocks
    private UserServiceImpl userServiceImplMock;

    //Test save user
    @Test
    void test1_saveUser_test2() {

        String id = "bruh";
        String username = "testuser";
        String fullname = "a";
        String password = "password";
        String phone = "123";
        String address = "hanoi";
        String email = "minh@gmail.com";
        String ip = "1.1.1.1";
        Account account = new Account(id, fullname, username, password, phone, address, email, ip, UserStatus.ACTIVE);

        Account savedAccount = userServiceImpl.saveUser(account);
        Account savedAccountGotFromDB = userRepository.getByUsername(username).get();

        assertNotNull(savedAccount);
        assertEquals(savedAccount.getId(), savedAccountGotFromDB.getId());
        assertEquals(savedAccount.getUsername(), savedAccountGotFromDB.getUsername());
        assertEquals(savedAccount.getPassword(), savedAccountGotFromDB.getPassword());
        assertEquals(savedAccount.getFullname(), savedAccountGotFromDB.getFullname());
        assertEquals(savedAccount.getPhone(), savedAccountGotFromDB.getPhone());
        assertEquals(savedAccount.getAddress(), savedAccountGotFromDB.getAddress());
        assertEquals(savedAccount.getEmail(), savedAccountGotFromDB.getEmail());
        assertEquals(savedAccount.getIp(), savedAccountGotFromDB.getIp());
        assertEquals(savedAccount.getStatus(), savedAccountGotFromDB.getStatus());

    }

    //Test save user update người dùng có sẵn
    @Test
    void test39_saveUser_test2() {

        String username = "user_1";
        Account user = userRepository.getByUsername(username).get();
        String accountName = user.getFullname();
        user.setFullname("brih");

        Account updatedUser = userServiceImpl.saveUser(user);
        Account updatedUserInDB = userRepository.getByUsername(username).get();

        assertNotNull(updatedUser);
        assertNotEquals(accountName, updatedUserInDB.getFullname());

    }

    //Test lấy user có trong db
    @Test
    void test2_getUserByUsername_test1() {

        String username = "super_admin.1234";

        AccountSummaryResponse gotAccount = userServiceImpl.getUserByUsername(username);
        AccountSummaryResponse expectedAccount = new AccountSummaryResponse(userRepository.getByUsername(username).get());

        assertNotNull(gotAccount);
        assertEquals(expectedAccount.getUsername(), gotAccount.getUsername());
        assertEquals(expectedAccount.getId(), gotAccount.getId());

    }

    //Test lấy user không có trong db
    @Test
    void test3_getUserByUsername_test2() {

        String username = "user_3";

        assertThrows(
                UsernameNotFoundException.class,
                () -> userServiceImpl.getUserByUsername(username)
        );

    }

    //Test add role admin to user
    @Test
    void test4_addRoleToUser_test1() {

        String testUser = "user_1";

        //Act
        userServiceImpl.addRoleToUser(testUser, ERole.ROLE_ADMIN);
        Account user = userRepository.getByUsername(testUser).get();

        //Assert
        assertFalse(user.getRoles().isEmpty());
        assertEquals(2, user.getRoles().size());

    }

    //Test add role trùng lặp
    @Test
    void test40_addRoleToUser_test2() {

        String testUser = "user_1";

        //Act
        userServiceImpl.addRoleToUser(testUser, ERole.ROLE_USER);
        Account user = userRepository.getByUsername(testUser).get();

        //Assert
        assertFalse(user.getRoles().isEmpty());
        assertEquals(1, user.getRoles().size());
    }

    //Test get all users
    @Test
    void test5_getUsers() {

        List<Account> accountList = userRepository.findAll();
        List<AccountSummaryResponse> actualAccountList = userServiceImpl.getUsers();

        assertFalse(actualAccountList.isEmpty());
        assertEquals(accountList.size(), actualAccountList.size());

    }

    //Test loadUserByUsername when using on user authority
    @Test
    void test6_loadUserByUsername_test1() {

        String username = "user_1";

        Account user = userRepository.getByUsername(username).get();
        UserDetails userDetails = userServiceImpl.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(user.getUsername(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertEquals(user.getAuthorities().size(), userDetails.getAuthorities().size());

    }

    //Test loadUserByUsername when using on admin authority
    @Test
    void test7_loadUserByUsername_test2() {

        String username = "admin.1234";

        Account user = userRepository.getByUsername(username).get();
        UserDetails userDetails = userServiceImpl.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(user.getUsername(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertEquals(user.getAuthorities().size(), userDetails.getAuthorities().size());

    }

    //Test loadUserByUsername when using on super admin authority
    @Test
    void test8_loadUserByUsername_test3() {

        String username = "super_admin.1234";

        Account user = userRepository.getByUsername(username).get();
        UserDetails userDetails = userServiceImpl.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(user.getUsername(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertEquals(user.getAuthorities().size(), userDetails.getAuthorities().size());

    }

    //Username khống chế với regex 5 chữ cái trở lên, có ký tự hoa, thường và số, có dấu . hoặc _
    //Test username không đúng regex
    @Test
    void test9_UsernameIsExisted_test1() {

        String username = "abc!";
        assertThrows(
                MyBadRequestException.class,
                () -> userServiceImpl.UsernameIsExisted(username)
        );

    }

    //Test username đúng regex nhưng đã có trong db
    @Test
    void test10_UsernameIsExisted_test2() {

        String username = "user_1";

        Boolean result = userServiceImpl.UsernameIsExisted(username);

        assertTrue(result);

    }

    //Test username đúng regex và không có trong db
    @Test
    void test11_UsernameIsExisted_test3() {

        String username = "jmaksldjkalsd";

        Boolean result = userServiceImpl.UsernameIsExisted(username);

        assertFalse(result);
    }

    //Email có định dạng là ít nhất 1 ký tự mở đầu rồi có @ và có ít nhất 1 ký tự đằng sau
    //Test email sai regex
    @Test
    void test12_EmailIsExisted_test1() {

        String email = "a";

        assertThrows(
                MyBadRequestException.class,
                () -> userServiceImpl.EmailIsExisted(email)
        );

    }

    //Test email đúng regex và có trong db
    @Test
    void test13_EmailIsExisted_test2() {

        String email = "vA@gmail.com";

        Boolean result = userServiceImpl.EmailIsExisted(email);

        assertTrue(result);

    }

    //Test email đúng regex và không có trong db
    @Test
    void test14_EmailIsExisted_test3() {

        String email = "a@gmil.com";

        Boolean result = userServiceImpl.EmailIsExisted(email);

        assertFalse(result);
    }

    //Password ít nhất dài 8 ký tự, có 1 ký tự hoa và thường, ít nhất 1 số và 1 ký tự đặc biệt
    //Test password rỗng
    @Test
    void test15_PasswordIsGood_test1() {

        String password = "";

        assertThrows(
                MyBadRequestException.class,
                () -> userServiceImpl.PasswordIsGood(password)
        );

    }

    //Test password sai regex
    @Test
    void test16_PasswordIsGood_test2() {

        String password = "1";

        assertThrows(
                MyBadRequestException.class,
                () -> userServiceImpl.PasswordIsGood(password)
        );

    }

    //Test password đúng regex
    @Test
    void test17_PasswordIsGood_test3() {

        String password = "oit#FI7%m6NYt5l";

        Boolean result = userServiceImpl.PasswordIsGood(password);

        assertTrue(result);
    }

    //Test getRawUserByUsername khi username không có trong db
    @Test
    void test18_getRawUserByUsername_test1() {

        String username = "user_3";

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userServiceImpl.getRawUserByUsername(username)
        );

        assertNotNull(exception);

    }

    //Test getRawUserByUsername khi username có trong db
    @Test
    void test19_getRawUserByUsername_test2() {

        String username = "user_1";

        Account user = userServiceImpl.getRawUserByUsername(username);
        Account expectedUser = userRepository.getByUsername(username).get();

        assertNotNull(user);
        assertEquals(expectedUser, user);

    }

    //Test getUserByName khi name rỗng
    @Test
    void test20_getUserByName_test1() {

        String name = "";

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userServiceImpl.getUserByName(name)
        );

        assertNotNull(exception);

    }

    //Test getUserByName khi name không có trong db
    @Test
    void test21_getUserByName_test2() {

        String name = "abc";

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userServiceImpl.getUserByName(name)
        );

        assertNotNull(exception);

    }

    //Test getUserByName khi name có trong db
    //Hàm này đặt tên hàm sai với mục đích của hàm
    @Test
    void test22_getUserByName_test3() {

        String name = "user_1";

        AccountSummaryResponse user = userServiceImpl.getUserByName(name);

        assertNotNull(user);
        assertEquals(name, user.getUsername());

    }

    //Test getUserByEmail khi email rỗng
    @Test
    void test23_getUserByEmail_test1() {

        String email = "";

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userServiceImpl.getUserByEmail(email)
        );

        assertNotNull(exception);

    }

    //Test getUserByEmail khi email không có trong db
    @Test
    void test24_getUserByEmail_test2() {

        String email = "minh@gmail.com";

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userServiceImpl.getUserByEmail(email)
        );

        assertNotNull(exception);

    }

    //Test getUserByEmail khi email có trong db
    @Test
    void test25_getUserByEmail_test3() {

        String email = "nguyenductan04202@gmail.com";

        AccountSummaryResponse user = userServiceImpl.getUserByEmail(email);

        assertNotNull(user);
        assertEquals(email, user.getEmail());

    }

    //Test searchByName khi string rỗng
    @Test
    void test26_searchByName_test1() {

        String search = "";

        List<AccountSummaryResponse> userList = userServiceImpl.searchByName(search);

        assertNotNull(userList);
        assertTrue(userList.isEmpty());

    }

    //Test searchByName khi tìm thấy người dùng
    //Hàm tìm kiếm theo tên người dùng này không check xem tên người dùng có chứa từ khóa hay không ví dụ search I Am ra các tên có chứa từ I Am trong đó
    @Test
    void test27_searchByName_test2() {

        String search = "I Am Super Admin";

        List<AccountSummaryResponse> userList = userServiceImpl.searchByName(search);

        assertNotNull(userList);
        assertFalse(userList.isEmpty());
        assertEquals(1, userList.size());

    }

    //Test searchByName khi không tìm thấy người dùng
    @Test
    void test28_searchByName_test3() {

        String search = "I Am";

        List<AccountSummaryResponse> userList = userServiceImpl.searchByName(search);

        assertNotNull(userList);
        assertTrue(userList.isEmpty());

    }

    //Test delete user
    @Test
    void test29_deteleUserByUsername_test1() {

        String search = "user_1";

        userServiceImpl.deteleUserByUsername(search);
        Account deletedUser = userRepository.getByUsername(search).orElse(null);

        assertNull(deletedUser);

    }

    //Test getRoleFromUser khi username rỗng
    @Test
    void test30_getRoleFromUser_test1() {

        String username = "";

        assertThrows(
                UsernameNotFoundException.class,
                () -> userServiceImpl.getRoleFromUser(username)
        );

    }

    //Test getRoleFromUser khi username không có trong db
    @Test
    void test31_getRoleFromUser_test2() {

        String username = "ajmksdmjaklsd";

        assertThrows(
                UsernameNotFoundException.class,
                () -> userServiceImpl.getRoleFromUser(username)
        );

    }

    //Test getRoleFromUser khi username có trong db
    @Test
    void test32_getRoleFromUser_test3() {

        String username = "super_admin.1234";

        Collection<Role> userRole = userServiceImpl.getRoleFromUser(username);

        assertNotNull(userRole);
        assertFalse(userRole.isEmpty());
        assertEquals(3, userRole.size());

    }

    //Test userHaveRole với người dùng không có role admin hoặc super admin
    @Test
    void test33_userHaveRole_test1() {

        String username = "user_1";

        Boolean haveRoleAdminTest = userServiceImpl.userHaveRole(username, ERole.ROLE_ADMIN);
        Boolean haveRoleSuperAdminTest = userServiceImpl.userHaveRole(username, ERole.ROLE_SUPER_ADMIN);

        assertFalse(haveRoleAdminTest);
        assertFalse(haveRoleSuperAdminTest);

    }

    //Test userHaveRole với người dùng không có role nào
    @Test
    void test34_userHaveRole_test2() {

        String username = "testUser";
        Account mockAccount = mock(Account.class);

        when(userRepositoryMock.getByUsername(username)).thenReturn(Optional.of(mockAccount));

        Boolean haveNoRoleTest1 = userServiceImplMock.userHaveRole(username, ERole.ROLE_USER);
        Boolean haveNoRoleTest2 = userServiceImplMock.userHaveRole(username, ERole.ROLE_ADMIN);
        Boolean haveNoRoleTest3 = userServiceImplMock.userHaveRole(username, ERole.ROLE_SUPER_ADMIN);

        assertFalse(haveNoRoleTest1);
        assertFalse(haveNoRoleTest2);
        assertFalse(haveNoRoleTest3);
        verify(userRepositoryMock, atLeastOnce()).getByUsername(username);

    }

    //Test userHaveRole với người dùng có role 
    @Test
    void test35_userHaveRole_test3() {

        String username = "user_1";

        Boolean haveRoleUserTest = userServiceImpl.userHaveRole(username, ERole.ROLE_USER);

        assertTrue(haveRoleUserTest);

    }

    //Test userHaveRole với account có role
    @Test
    void test36_userHaveRole_test4() {

        Account user = mock(Account.class);
        Collection<Role> roles = new ArrayList<>();
        roles.add(new Role(ERole.ROLE_USER));
        roles.add(new Role(ERole.ROLE_ADMIN));
        roles.add(new Role(ERole.ROLE_SUPER_ADMIN));

        when(user.getRoles()).thenReturn(roles);

        Boolean userHaveUserRoleTest = userServiceImplMock.userHaveRole(user, ERole.ROLE_USER);
        Boolean userHaveAdminRoleTest = userServiceImplMock.userHaveRole(user, ERole.ROLE_ADMIN);
        Boolean userHaveSuperAdminRoleTest = userServiceImplMock.userHaveRole(user, ERole.ROLE_SUPER_ADMIN);

        assertTrue(userHaveUserRoleTest);
        assertTrue(userHaveAdminRoleTest);
        assertTrue(userHaveSuperAdminRoleTest);

    }

    //Test userHaveRole với account không có role
    @Test
    void test37_userHaveRole_test5() {

        Account user = mock(Account.class);
        Collection<Role> roles = new ArrayList<>();

        when(user.getRoles()).thenReturn(roles);

        Boolean userHaveUserRoleTest = userServiceImplMock.userHaveRole(user, ERole.ROLE_USER);
        Boolean userHaveAdminRoleTest = userServiceImplMock.userHaveRole(user, ERole.ROLE_ADMIN);
        Boolean userHaveSuperAdminRoleTest = userServiceImplMock.userHaveRole(user, ERole.ROLE_SUPER_ADMIN);

        assertFalse(userHaveUserRoleTest);
        assertFalse(userHaveAdminRoleTest);
        assertFalse(userHaveSuperAdminRoleTest);

    }

    //Test removeRoleUser mặc định là username truyền vào tìm kiếm không bị throws exception
    @Test
    void test38_removeRoleUser_test1() {

        String username = "super_admin.1234";

        userServiceImpl.removeRoleUser(username, ERole.ROLE_USER);
        Account user = userRepository.getByUsername(username).get();

        assertFalse(user.getRoles().isEmpty());
        assertEquals(2, user.getRoles().size());

    }

}
