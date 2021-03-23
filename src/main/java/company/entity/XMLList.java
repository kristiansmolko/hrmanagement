package company.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "users")
public class XMLList {
    private List<User> list;

    public List<User> getList() {
        return list;
    }

    @XmlElement(name = "user")
    public void setList(List<User> list) {
        this.list = list;
    }
}
