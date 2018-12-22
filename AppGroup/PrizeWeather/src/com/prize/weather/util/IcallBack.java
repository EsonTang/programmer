package com.prize.weather.util;

// move
public interface IcallBack {
	public void OnMoveStop(int WeatherCode);
	public void OnMoveRestart(int WeatherCode);
	public void clearAnimation();
}
