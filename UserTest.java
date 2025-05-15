package cinema.ticket.booking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import cinema.ticket.booking.exception.MyBadRequestException;
import cinema.ticket.booking.exception.MyNotFoundException;
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

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Test
    void USER_001_saveUser_test01() {

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

    @Test
    void USER_002_saveUser_test02() {
        String user_id = "fa19dcbd-241797c7-b83f36c5";
        String username = "brih";

        Account account = userRepository.findById(user_id).get();
        account.setUsername(username);

        userServiceImpl.saveUser(account);
        Account savedAccount = userRepository.findById(user_id).get();

        assertNotNull(savedAccount);
        assertEquals(user_id, savedAccount.getId());
        assertEquals(username, savedAccount.getUsername());

    }

    @Test
    void USER_003_saveRole_test01() {
        long role_id = 4;

        Role role = userServiceImpl.saveRole(new Role(role_id, ERole.ROLE_USER));
        Role savedRole = roleRepository.findById(role_id).orElse(null);

        assertNotNull(savedRole);
        assertEquals(role_id, savedRole.getId());
        assertEquals(ERole.ROLE_USER.name(), savedRole.getRole());
    }

    @Test
    void USER_004_saveRole_test02() {
        long role_id = 1;

        Role role = roleRepository.findById(role_id).get();
        role.setRole(ERole.ROLE_USER);

        userServiceImpl.saveRole(role);
        Role savedRole = roleRepository.findById(role_id).get();

        assertNotNull(savedRole);
        assertEquals(role_id, savedRole.getId());
        assertEquals(ERole.ROLE_USER.name(), savedRole.getRole());

    }

    @Test
    void USER_005_getUserByUsername_test01() {
        String username = "";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> userServiceImpl.getUserByUsername(username)
        );

        String expectedMessage = "Username must not be empty";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_006_getUserByUsername_test02() {
        String username = "abc!";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> userServiceImpl.getUserByUsername(username)
        );

        String expectedMessage = "Username is invalid. Username must be at least 5 characters long; Password can have lowercase, uppercase, numbers, character . or _";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_007_getUserByUsername_test03() {
        String username = "user_3";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> userServiceImpl.getUserByUsername(username)
        );

        String expectedMessage = "User not found";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_008_getUserByUsername_test04() {
        String username = "avart";

        AccountSummaryResponse account = userServiceImpl.getUserByUsername(username);

        assertNotNull(account);
        assertEquals(username, account.getUsername());

    }

    @Test
    void USER_009_addRoleToUser_test01() {
        String username = "user_3";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> userServiceImpl.addRoleToUser(username, ERole.ROLE_USER)
        );

        String expectedMessage = "User not found";
        assertEquals(expectedMessage, exception.getMessage());

    }

    @Test
    void USER_010_addRoleToUser_test02() {
        String username = "abc!";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> userServiceImpl.addRoleToUser(username, ERole.ROLE_USER)
        );

        String expectedMessage = "Username is invalid. Username must be at least 5 characters long; Password can have lowercase, uppercase, numbers, character . or _";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_011_addRoleToUser_test03() {
        String username = "user_3";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> userServiceImpl.addRoleToUser(username, ERole.ROLE_USER)
        );

        String expectedMessage = "User not found";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_012_addRoleToUser_test04() {
        String user_id = "fa19dcbd-241797c7-bA83f36c5";
        String username = "user_1";
        ERole role = ERole.ROLE_ADMIN;

        userServiceImpl.addRoleToUser(username, role);
        Account savedAccount = userRepository.getByUsername(username).get();

        assertEquals(2, savedAccount.getRoles().size());
        assertTrue(savedAccount.getRoles().stream().anyMatch(r -> r.getRole().equals(role.name())));
        assertTrue(savedAccount.getRoles().stream().anyMatch(r -> r.getRole().equals(ERole.ROLE_USER.name())));

    }

    @Test
    void USER_013_addRoleToUser_test05() {
        String user_id = "fa19dcbd-241797c7-bA83f36c5";
        String username = "user_1";
        ERole role = ERole.ROLE_USER;

        userServiceImpl.addRoleToUser(username, role);
        Account savedAccount = userRepository.getByUsername(username).get();

        assertEquals(1, savedAccount.getRoles().size());
        assertTrue(savedAccount.getRoles().stream().anyMatch(r -> r.getRole().equals(role.name())));
        assertFalse(savedAccount.getRoles().stream().anyMatch(r -> r.getRole().equals(ERole.ROLE_ADMIN.name())));
        assertFalse(savedAccount.getRoles().stream().anyMatch(r -> r.getRole().equals(ERole.ROLE_SUPER_ADMIN.name())));
    }

    @Test
    void USER_014_getUsers() {

        List<AccountSummaryResponse> users = userServiceImpl.getUsers();

        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertEquals(5, users.size());
    }

    @Test
    void USER_015_loadUserByUsername_test01() {
        String username = "";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> userServiceImpl.loadUserByUsername(username)
        );

        String expectedMessage = "Username must not be empty";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_016_loadUserByUsername_test02() {
        String username = "abc!";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> userServiceImpl.loadUserByUsername(username)
        );

        String expectedMessage = "Username is invalid. Username must be at least 5 characters long; Password can have lowercase, uppercase, numbers, character . or _";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_017_loadUserByUsername_test03() {
        String username = "user_3";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> userServiceImpl.loadUserByUsername(username)
        );

        String expectedMessage = "User not found";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_018_loadUserByUsername_test04() {
        String username = "super_admin.1234";

        UserDetails userDetails = userServiceImpl.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals(3, userDetails.getAuthorities().size());
    }

    @Test
    void USER_019_usernameIsExisted_test01() {
        String username = "";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> userServiceImpl.UsernameIsExisted(username)
        );

        String expectedMessage = "Username must not be empty";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_020_usernameIsExisted_test02() {
        String username = "abc!";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> userServiceImpl.UsernameIsExisted(username)
        );

        String expectedMessage = """
                                 Username is unvalid. Username must follow these requirements:\r
                                  + At least 5 characters long\r
                                  + No whitespace and special character, except . and _""" //
                //
                ;
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_021_usernameIsExisted_test03() {
        String username = "user_3";

        Boolean isExisted = userServiceImpl.UsernameIsExisted(username);

        assertNotNull(isExisted);
        assertFalse(isExisted);
    }

    @Test
    void USER_022_usernameIsExisted_test04() {
        String username = "user_1";

        Boolean isExisted = userServiceImpl.UsernameIsExisted(username);

        assertNotNull(isExisted);
        assertTrue(isExisted);
    }

    @Test
    void USER_023_emailIsExisted_test01() {
        String email = "";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> userServiceImpl.EmailIsExisted(email)
        );

        String expectedMessage = "Email is invalid";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_024_emailIsExisted_test02() {
        String email = "a@";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> userServiceImpl.EmailIsExisted(email)
        );

        String expectedMessage = "Email is invalid";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_025_emailIsExisted_test03() {
        String email = "minh@gmail.com";

        Boolean isExisted = userServiceImpl.EmailIsExisted(email);
        assertNotNull(isExisted);
        assertFalse(isExisted);
    }

    @Test
    void USER_026_emailIsExisted_test04() {
        String email = "vA@gmail.com";

        Boolean isExisted = userServiceImpl.EmailIsExisted(email);
        assertNotNull(isExisted);
        assertTrue(isExisted);
    }

    @Test
    void USER_027_passwordIsGood_test01() {
        String password = "";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> userServiceImpl.PasswordIsGood(password)
        );

        String expectedMessage = "Password must not be empty";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_028_passwordIsGood_test02() {
        String password = "1";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> userServiceImpl.PasswordIsGood(password)
        );

        String expectedMessage = "Password is invalid. Password must have:\n + At least 8 characters long\n + Contains at least one uppercase letter\n + Contains at least one lowercase letter\n + Contains at least one digit\n + Contains at least one special character\n";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_029_passwordIsGood_test03() {
        String password = "123456789";

        Boolean isGood = userServiceImpl.PasswordIsGood(password);

        assertNotNull(isGood);
        assertFalse(isGood);

    }

    @Test
    void USER_030_passwordIsGood_test04() {
        String password = "oit#FI7%m6NYt5l";

        Boolean isGood = userServiceImpl.PasswordIsGood(password);

        assertNotNull(isGood);
        assertTrue(isGood);
    }

    @Test
    void USER_031_getUserByName_test01() {
        String username = "";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> userServiceImpl.getUserByName(username)
        );

        String expectedMessage = "Username must not be empty";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_032_getUserByName_test02() {
        String username = "abc!";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> userServiceImpl.getUserByName(username)
        );

        String expectedMessage = "Username is invalid. Username must be at least 5 characters long; Password can have lowercase, uppercase, numbers, character . or _";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_033_getUserByName_test03() {
        String name = "I Am User 1";
        String user_id = "fa19dcbd-241797c7-bA83f36c5";

        AccountSummaryResponse account = userServiceImpl.getUserByName(name);

        assertNotNull(account);
        assertEquals(user_id, account.getId());
        assertEquals(name, account.getUsername());
    }

    @Test
    void USER_034_getUserByEmail_test01() {
        String email = "";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> userServiceImpl.getUserByEmail(email)
        );

        String expectedMessage = "Email must not empty";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_035_getUserByEmail_test02() {
        String email = "a@";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> userServiceImpl.getUserByEmail(email)
        );

        String expectedMessage = "Email is invalid";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_036_getUserByEmail_test03() {
        String email = "minh@gmail.com";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> userServiceImpl.getUserByEmail(email)
        );

        String expectedMessage = "User not found";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_037_getUserByEmail_test04() {
        String email = "vA@gmail.com";
        String user_id = "562db9be-1e0c9cc6-66569a1b";

        AccountSummaryResponse account = userServiceImpl.getUserByEmail(email);
        assertNotNull(account);
        assertEquals(email, account.getEmail());
        assertEquals(user_id, account.getId());
    }

    @Test
    void USER_038_searchByName_test01() {
        String username = "";

        List<AccountSummaryResponse> accounts = userServiceImpl.searchByName(username);
        assertNotNull(accounts);
        assertTrue(accounts.isEmpty());
    }

    @Test
    void USER_039_searchByName_test02() {
        String username = "I Am";

        List<AccountSummaryResponse> accounts = userServiceImpl.searchByName(username);
        assertNotNull(accounts);
        assertFalse(accounts.isEmpty());
        assertEquals(4, accounts.size());
    }

    @Test
    void USER_040_searchByName_test03() {
        String username = "Super Admin";

        List<AccountSummaryResponse> accounts = userServiceImpl.searchByName(username);
        assertNotNull(accounts);
        assertFalse(accounts.isEmpty());
        assertEquals(1, accounts.size());
    }

    @Test
    void USER_041_deleteUserByUsername_test01() {
        String username = "";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> userServiceImpl.deteleUserByUsername(username)
        );

        String expectedMessage = "Username must not be empty";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_042_deleteUserByUsername_test02() {
        String username = "user_3";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> userServiceImpl.deteleUserByUsername(username)
        );

        String expectedMessage = "User not found";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_043_deleteUserByUsername_test03() {
        String username = "user_1";
        String user_id = "fa19dcbd-241797c7-bA83f36c5";

        userServiceImpl.deteleUserByUsername(username);
        Account account = userRepository.findById(user_id).orElse(null);

        assertNull(account);
    }

    @Test
    void USER_044_getRoleFromUser_test01() {
        String username = "";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> userServiceImpl.getRoleFromUser(username)
        );

        String expectedMessage = "Username must not be empty";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_045_getRoleFromUser_test02() {
        String username = "user_3";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> userServiceImpl.getRoleFromUser(username)
        );

        String expectedMessage = "User not found";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_046_getRoleFromUser_test03() {
        String username = "user_1";
        String user_id = "fa19dcbd-241797c7-bA83f36c5";

        Collection<Role> roles = userServiceImpl.getRoleFromUser(username);
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertEquals(1, roles.size());

    }

    @Test
    void USER_047_userHaveRole_test01() {
        String username = "";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> userServiceImpl.userHaveRole(username, ERole.ROLE_USER)
        );

        String expectedMessage = "Username must not be empty";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_048_userHaveRole_test02() {
        String username = "user_3";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> userServiceImpl.userHaveRole(username, ERole.ROLE_USER)
        );

        String expectedMessage = "User not found";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_049_userHaveRole_test03() {
        String username = "user_1";
        ERole role = ERole.ROLE_USER;

        Boolean isHaveRole = userServiceImpl.userHaveRole(username, role);
        assertNotNull(isHaveRole);
        assertTrue(isHaveRole);
    }

    @Test
    void USER_050_userHaveRole_test04() {
        String username = "user_1";
        ERole role = ERole.ROLE_SUPER_ADMIN;

        Boolean isHaveRole = userServiceImpl.userHaveRole(username, role);
        assertNotNull(isHaveRole);
        assertFalse(isHaveRole);
    }

    @Test
    void USER_051_userHaveRole_test05() {
        String username = "user_1";
        String user_id = "fa19dcbd-241797c7-b83f36c5";

        Account user = userRepository.findById(user_id).get();
        user.setRoles(new ArrayList<>());
        userServiceImpl.saveUser(user);

        ERole user_role = ERole.ROLE_USER;
        ERole admin_role = ERole.ROLE_ADMIN;
        ERole super_admin_role = ERole.ROLE_SUPER_ADMIN;

        Boolean isHaveUserRole = userServiceImpl.userHaveRole(username, user_role);
        Boolean isHaveAdminRole = userServiceImpl.userHaveRole(username, admin_role);
        Boolean isHaveSuperAdminRole = userServiceImpl.userHaveRole(username, super_admin_role);

        assertNotNull(isHaveUserRole);
        assertNotNull(isHaveAdminRole);
        assertNotNull(isHaveSuperAdminRole);
        assertFalse(isHaveUserRole);
        assertFalse(isHaveAdminRole);
        assertFalse(isHaveSuperAdminRole);

    }

    @Test
    void USER_052_removeRoleUser_test01() {
        String username = "";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> userServiceImpl.removeRoleUser(username, ERole.ROLE_USER)
        );

        String expectedMessage = "Username must not be empty";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_053_removeRoleUser_test02() {
        String username = "user_3";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> userServiceImpl.removeRoleUser(username, ERole.ROLE_USER)
        );

        String expectedMessage = "User not found";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void USER_054_removeRoleUser_test03() {
        String username = "super_admin.1234";
        ERole role = ERole.ROLE_USER;

        userServiceImpl.removeRoleUser(username, role);
        Account savedAccount = userRepository.getByUsername(username).get();

        assertNotNull(savedAccount);
        assertEquals(2, savedAccount.getRoles().size());
    }

    @Test
    void USER_055_removeRoleUser_test04() {
        String username = "user_1";
        ERole role = ERole.ROLE_ADMIN;

        userServiceImpl.removeRoleUser(username, role);
        Account savedAccount = userRepository.findById("fa19dcbd-241797c7-b83f36c5").get();

        assertNotNull(savedAccount);
        assertEquals(1, savedAccount.getRoles().size());
    }
}
