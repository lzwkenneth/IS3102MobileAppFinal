package kenneth.jf.siaapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import static android.R.attr.name;
import static android.R.id.message;

/**
 * Created by Kenneth on 13/10/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventListObject {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("event_title")
    private String eventName;
    @JsonProperty("hasTicket")
    private boolean hasTicket;
    @JsonProperty("filePath")
    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isHasTicket() {
        return hasTicket;
    }

    public void setHasTicket(boolean hasTicket) {
        this.hasTicket = hasTicket;
    }

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String toString(){
        return "ID is " + id + " Name is " + eventName;
    }

}
