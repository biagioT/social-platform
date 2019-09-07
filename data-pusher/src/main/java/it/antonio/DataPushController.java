package it.antonio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.antonio.datapush.DataSender;
import it.antonio.datapush.TestData;

@RestController
public class DataPushController {
    
	@Autowired
	DataSender sender;
 
    @PostMapping("/data-push")
    public String homePage(@RequestBody TestData data ) {
    	sender.sendData(data);
        return "ok";
    }
}

