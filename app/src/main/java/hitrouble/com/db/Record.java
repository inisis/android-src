package hitrouble.com.db;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * @ProjectName: CarPlate
 * @Package: hitrouble.com.db
 * @ClassName: Record
 * @Description:
 * @Author: kevin
 * @CreateDate: 2019/4/13 1:01 PM
 */
@Entity
public class Record {
    @Id(assignable = true)
    public long busCode;
    public boolean submited;
    public String plateImage;
    public String otherImage;
    public String plateValue;
    public String address;
    public long time;
    public double latitude;
    public double longitude;
}
