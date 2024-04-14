package projector.model;

import com.j256.ormlite.field.DatabaseField;

import static projector.utils.TextEncoder.decode;
import static projector.utils.TextEncoder.encode;

public class LoggedInUser extends BaseEntity {

    @DatabaseField
    private String email;
    @DatabaseField
    private String password;
    @DatabaseField
    private String surname;
    @DatabaseField
    private String firstName;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return decode(password);
    }

    public void setPassword(String password) {
        this.password = encode(password);
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}
