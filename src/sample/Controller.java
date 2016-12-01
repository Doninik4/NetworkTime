package sample;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXSlider;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.WinUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.TimeZone;

public class Controller {

    @FXML
    private JFXDatePicker dateSelector;

    @FXML
    private JFXSlider hourSlider;

    @FXML
    private JFXSlider minuteSlider;


    @FXML
    protected void setSelectedTime(ActionEvent event){
        LocalDate localDate = dateSelector.getValue();
        if(localDate == null){
            return;
        }
        int hour = (int) Math.round(hourSlider.getValue());
        int minute = (int)Math.round(minuteSlider.getValue());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        calendar.set(localDate.getYear(), localDate.getMonthValue() -1, localDate.getDayOfMonth(), hour, minute);
        long unixTime = calendar.getTimeInMillis() /1000;
        System.out.println("Set time to" + unixTime);
        Shell32.INSTANCE.ShellExecute(null, "runas", "ChangeSystemTime.exe", Long.toString(unixTime), null, WinUser.SW_SHOWNORMAL);
    }



}
