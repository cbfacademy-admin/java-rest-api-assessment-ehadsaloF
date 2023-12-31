package com.cbfacademy.apiassessment.Service;

import com.cbfacademy.apiassessment.DTO.UserDTO;
import com.cbfacademy.apiassessment.Entity.User;
import com.cbfacademy.apiassessment.Entity.UserRoles;
import com.cbfacademy.apiassessment.Mappers.UserMapper;
import com.cbfacademy.apiassessment.Repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.cbfacademy.apiassessment.Validators.ValidateArgs.isValidEmail;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{

    @Autowired
    UserRepository userRepository;

    UserMapper userMapper;


    /**
     * Saves a new user if the provided email and username do not already exist
     * Assigns the role USER to the new user
     *
     * @param user The user to be saved
     * @return The saved user
     * @throws EntityExistsException If a user with the same email or username already exists
     */
    @Override
    public User saveUser(User user)
            throws EntityExistsException{
        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            throw new EntityExistsException("User with email: " + user.getEmail() + " already exist");
        }
        if(userRepository.findByUsername(user.getUsername()).isPresent()){
            throw new EntityExistsException("User with username: " + user.getUsername() + " already exist");
        }
        user.setRole(UserRoles.USER);
        if(!isValidEmail(user.getEmail())){
            throw new ValidationException("Please Enter a valid Email");
        }
        return userRepository.save(user);
    }


    /**
     * Updates the name of a user identified by the provided username or email
     *
     * @param usernameOrEmail The username or email of the user to be updated
     * @param name The new name to set
     * @return The updated user
     * @throws EntityNotFoundException If the user does not exist
     */
    @Override
    public User updateUser(String usernameOrEmail, String name)
            throws EntityNotFoundException {
        User existingUser = getUserByUsernameOrEmail(usernameOrEmail);
            if (existingUser != null) {
                // Update the existing user with the new data
                existingUser.setName(name);
                existingUser.setUpdatedAt();
                return userRepository.save(existingUser);
            }
            throw new EntityNotFoundException("User Does Not Exist");
    }


    /**
     * Gets a user by username or email.
     *
     * @param usernameOrEmail The username or email of the user
     * @return the user
     * @throws EntityNotFoundException If the user does not exist
     */
    @Override
    public User getUserByUsernameOrEmail(String usernameOrEmail)
            throws EntityNotFoundException{
        Optional<User> existingUser = userRepository.findByUsername(usernameOrEmail);

        if (existingUser.isEmpty()) {
            existingUser = userRepository.findByEmail(usernameOrEmail);
        }
        if (existingUser.isEmpty()) {
            throw new EntityNotFoundException("User Does Not Exist");
        }

        return existingUser.orElse(null);
    }


    /**
     * Retrieves all users and writes them to a JSON file.
     *
     * @throws IOException If an error occurs while generating the file.
     * @throws EntityNotFoundException If no users are found
     */
    @Override
    public void getAllUsersAsJSONFile() throws IOException, EntityNotFoundException {
        List<UserDTO> userList = userRepository.findAll().stream().
                map(userMapper.INSTANCE::userDTO).
                collect(Collectors.toList());

        if(userList.isEmpty()) throw new EntityNotFoundException("No Users Found");


        String outputFile = "src/main/resources/AllUsers.JSON";
        Gson gson = new Gson();

        // Delete the existing file if it exists
        File file = new File(outputFile);
        if (file.exists()){
            file.delete();
        }

        try (FileWriter writer = new FileWriter(outputFile)) {
            // Create a new file
            gson.toJson(userList, writer);
        } catch (IOException e) {
            throw new IOException("Error generating File");
        }

    }


    /**
     * Retrieves all users.
     *
     * @return A list of all users.
     * @throws EntityNotFoundException - If no users are found
     */
    @Override
    public List<User> getAllUsers() throws EntityNotFoundException {
        List<User> users = userRepository.findAll();
        if(users.isEmpty()) throw new EntityNotFoundException("No Users Found");

        return users;
    }


    /**
     * Deletes a user identified by the provided username or email.
     *
     * @param usernameOrEmail The username or email of the user to be deleted.
     * @throws EntityNotFoundException If the user does not exist.
     */
    @Override
    public void deleteUser(String usernameOrEmail) throws EntityNotFoundException{
        User user = getUserByUsernameOrEmail(usernameOrEmail);
        if(user == null){
            throw new EntityNotFoundException("User Does not Exist");
        }
        userRepository.delete(user);
    }
}
