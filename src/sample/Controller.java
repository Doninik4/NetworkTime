package sample;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXSlider;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.WinUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
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
    protected void setSelectedTime(ActionEvent event) throws URISyntaxException, UnsupportedEncodingException {
        LocalDate localDate = dateSelector.getValue();
        if(localDate == null){
            return;
        }
        int hour = (int) Math.round(hourSlider.getValue());
        int minute = (int)Math.round(minuteSlider.getValue());
        Calendar calendar = Calendar.getInstance();
        calendar.set(localDate.getYear(), localDate.getMonthValue() -1, localDate.getDayOfMonth(), hour, minute);
        long unixTime = calendar.getTimeInMillis() /1000;
        System.out.println("Set time to" + unixTime);
        String urlOfProgram = new File(Controller.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getAbsoluteFile().getParent();
        urlOfProgram = URLDecoder.decode(urlOfProgram + File.separator + "ChangeSystemTime.exe" , "UTF-8");
        System.out.println(urlOfProgram);
        Shell32.INSTANCE.ShellExecute(null, "open", urlOfProgram , Long.toString(unixTime), null, WinUser.SW_SHOWNORMAL);
    }



}
