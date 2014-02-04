package com.lhings.jfokusdemo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.lhings.library.Action;
import com.lhings.library.Stats;
import com.lhings.library.LhingsDevice;
import com.lhings.library.Payload;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class RaspberryDemo extends LhingsDevice {

	boolean fanOn = false;

	final GpioController gpio = GpioFactory.getInstance();
	GpioPinDigitalOutput relay;

	public RaspberryDemo() {
		super("test", "test", 5000, "Raspberry Controller");

	}

	@Override
	public void setup() {
		// set up GPIO pin 6 on Raspberry as digital output pin
		relay = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06,
		"Fan_Relay",
		PinState.LOW);
	}

	@Override
	public void loop() {
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// ************ FAN CONTROL ********************
	@Action(name = "FanOn", description = "Turns on fan", argumentNames = {}, argumentTypes = {})
	public void fanOn() {
		System.out.println("Received fan on");
		relay.high();
		fanOn = true;
	}

	@Action(name = "FanOff", description = "Turns off fan", argumentNames = {}, argumentTypes = {})
	public void fanOff() {
		System.out.println("Received fan off");
		relay.low();
		fanOn = false;
	}

	@Stats(name = "Is Fan On", type = "boolean")
	public boolean fanOnOrOff(){
		return fanOn;
	}

	// *********** HUE CONTROL *********************

	@Action(name = "HueOn", description = "", argumentNames = {}, argumentTypes = {})
	public void on() {
		System.out.println("Received HueOn");
		String payload = "{\"on\":true}";
		callWebService(payload);
	}

	@Action(name = "HueOff", description = "", argumentNames = {}, argumentTypes = {})
	public void off() {
		System.out.println("Received HueOff");
		String payload = "{\"on\":false}";
		callWebService(payload);
	}

	@Action(name = "SetColor", description = "Used to make the Hue give temperature feedback", argumentNames = {}, argumentTypes = {})
	public void setHueColor(@Payload String strTemperature) {
		
		callWebService(Float.valueOf(strTemperature));
	}

	// ************* private methods (HUE related) ***************

	private void callWebService(float temperature) {
		float percentage = temperatureToPercentage(temperature);
		System.out.println("Temperature " + temperature + " is " + percentage + "%");
		percentage = percentage / 100;
		float x = percentage * 0.51f + 0.16f;
		float y = percentage * 0.27f + 0.05f;
		String payload = "{\"xy\":[" + x + ", " + y + "]}";
		callWebService(payload);
	}
	private void callWebService(String payload) {
		
		try {
			URL hueColorService = new URL(
					"http://192.168.1.129/api/jfokususer/lights/1/state");
			HttpURLConnection conn = (HttpURLConnection) hueColorService.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("PUT");
			conn.setRequestProperty("Content-Type", "application/json");

			String input = payload;

			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			conn.disconnect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 

	private float temperatureToPercentage(float x) {
		float in_min=30;
		float in_max=45;
		float out_min=0;
		float out_max=100;
		
		if (x<in_min)
			return out_min;
		if (x>in_max)
			return out_max;
		
		return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}


	public static void main(String[] args) {
		@SuppressWarnings("unused")
		// starting your device is as easy as creating an instance!!
		RaspberryDemo demo = new RaspberryDemo();

	}

}