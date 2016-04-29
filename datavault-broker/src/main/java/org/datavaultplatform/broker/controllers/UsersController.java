package org.datavaultplatform.broker.controllers;

import java.util.HashMap;
import java.util.List;

import org.datavaultplatform.broker.services.FileStoreService;
import org.datavaultplatform.broker.services.UserKeyPairService;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.common.request.ValidateUser;
import org.jsondoc.core.annotation.*;
import org.jsondoc.core.pojo.ApiVerb;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(name="Users", description = "Interact with DataVault Users")
public class UsersController {
    
    private UsersService usersService;
    private FileStoreService fileStoreService;
    private UserKeyPairService userKeyPairService;
    private String activeDir;

    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }
    public void setFileStoreService(FileStoreService fileStoreService) {
        this.fileStoreService = fileStoreService;
    }
    public void setUserKeyPairService(UserKeyPairService userKeyPairService) {
        this.userKeyPairService = userKeyPairService;
    }
    public void setActiveDir(String activeDir) {
        this.activeDir = activeDir;
    }


    @ApiMethod(
            path = "/users/{userid}",
            verb = ApiVerb.GET,
            description = "Get a specific DataVault User",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/users/{userid}", method = RequestMethod.GET)
    public User getUser(@PathVariable("userid") @ApiPathParam(name = "User ID", description = "The User ID to retrieve") String queryUserID) {
        return usersService.getUser(queryUserID);
    }

    @ApiMethod(
            path = "/users}",
            verb = ApiVerb.POST,
            description = "Create a new DataVault User",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/users", method = RequestMethod.POST)
    public User addUser(@RequestHeader(value = "X-UserID", required = true) String userID,
                        @RequestBody User user) throws Exception {
        usersService.addUser(user);

        // Add default fileStores for the new user?

        return user;
    }

    @ApiMethod(
            path = "/users}",
            verb = ApiVerb.PUT,
            description = "Edit a DataVault User",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/users", method = RequestMethod.PUT)
    public User editUser(@RequestHeader(value = "X-UserID", required = true) String userID,
                         @RequestBody User user) throws Exception {


        usersService.updateUser(user);

        return user;
    }


    @ApiMethod(
            path = "/users/{userid}/keys",
            verb = ApiVerb.GET,
            description = "Check to see if keys exist for user",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/users/{userid}/keys", method = RequestMethod.GET)
    public Boolean keysExist(@PathVariable("userid") @ApiPathParam(name = "User ID", description = "Do keys already exist for this user ID?") String userID ) throws Exception {

        User user = usersService.getUser(userID);

        FileStore store = null;
        String privateKey = null;
        List<FileStore> userStores = user.getFileStores();
        for (FileStore userStore : userStores) {
            if (userStore.getStorageClass().equals("org.datavaultplatform.common.storage.impl.SFTPFileSystem")) {
                store = userStore;
                privateKey = store.getProperties().get("privateKey");
            }
        }

        if (privateKey != null) {
            return true;
        } else {
            // todo - should we set the return code to 404?
            return false;
        }
    }

    @ApiMethod(
            path = "/users/{userid}/keys",
            verb = ApiVerb.POST,
            description = "Generate a Key Pair for user",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/users/{userid}/keys", method = RequestMethod.POST)
    public String addKeyPair(@PathVariable("userid") @ApiPathParam(name = "User ID", description = "Get the Public Key for this user ID") String userID) throws Exception {
        User user = usersService.getUser(userID);
        userKeyPairService.generateNewKeyPair();

        HashMap<String,String> storeProperties = new HashMap<String,String>();
        storeProperties.put("host", "localhost");
        storeProperties.put("rootPath", activeDir);
        storeProperties.put("username", user.getID());
        storeProperties.put("password", "");
        storeProperties.put("privateKey", userKeyPairService.getPrivateKey());

        FileStore store = new FileStore("org.datavaultplatform.common.storage.impl.SFTPFileSystem", storeProperties, "SFTP filesystem");
        store.setUser(user);
        fileStoreService.addFileStore(store);

        return userKeyPairService.getPublicKey();
    }


    @RequestMapping(value = "/auth/users", method = RequestMethod.POST)
    public Boolean validateUser(@RequestBody ValidateUser validateUser) throws Exception {

        return usersService.validateUser(validateUser.getUserid(), validateUser.getPassword());
    }

    @RequestMapping(value = "/auth/users/isadmin", method = RequestMethod.POST)
    public Boolean isAdmin(@RequestBody ValidateUser validateUser) throws Exception {
        User user = usersService.getUser(validateUser.getUserid());
        return user.isAdmin();
    }


}
