package it.antonio.datapush;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.antonio.api.Survey;
import it.antonio.datapush.DataSender;

@RestController
public class DataPushController {
    
	@Autowired
	DataSender sender;
 
	@CrossOrigin("*")
    @PostMapping("/push-survey")
    public String survey(@RequestBody Survey data ) {
    	sender.sendData(data);
        return "ok";
    }
}
