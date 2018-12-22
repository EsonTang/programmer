
 /*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
*********************************************/

package com.prize.smart.gene;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;


/*
 * PrizeGeneHelper.lowPassFilter filter = new PrizeGeneHelper.lowPassFilter(0.01f);
 * out = filter.filter3d(in);
 * */
public class PrizeGeneHelper {
	private static final String TAG = "prize";
	
	/* filter */
	public static class lowPassFilter {
			/*
			 * 0 - weight filter
			 * 1 - moving average
			 * */
		private int type = 0;
		  public lowPassFilter(int type) {
			  this.type = type;
			  MovingAverageInit();
		  }
		  public lowPassFilter(int type, float w) {
			  __mLowPassWeight = w;
			  this.type = type;
			  MovingAverageInit();
		  }
		  public lowPassFilter(int type, int k) {
			  __mSMAk = k;
			  this.type = type;
			  MovingAverageInit();
		  }
		  public lowPassFilter(int type, int k, float w) {
			  __mSMAk = k;
			  __mLowPassWeight = w;
			  this.type = type;
			  MovingAverageInit();
		  }
		  
		// a low pass filter 
	  private float __mLowPassWeight = 0.01f;
	  private float[] __mLowPassLastValue = new float[3];

	  public void setWeight(float weight) {
		  __mLowPassWeight = weight;
	  }
	  public float filter(float cur, float last) {
		  return last*(1.0f - __mLowPassWeight) + cur*__mLowPassWeight;
	  }
	  public float[] filter(float[] in) {
		  float[] out = new float[3];
		  out[0] = filter(in[0], __mLowPassLastValue[0]);
		  out[1] = filter(in[1], __mLowPassLastValue[1]);
		  out[2] = filter(in[2], __mLowPassLastValue[2]);
		  __mLowPassLastValue[0] = in[0];
		  __mLowPassLastValue[1] = in[1];
		  __mLowPassLastValue[2] = in[2];
		  return out;
	  }
	  public float filter(float cur) {
		  return filter(cur, __mLowPassLastValue[0]);
	  }
	  
  		// simple moving average
	  private class MovingAverage {
		  private float cirBuf[];
		  private float avg;
		  private int cirIndex;
		  private int count;
		  public MovingAverage(int k) {
			  cirBuf = new float[k];
			  count = 0;
			  cirIndex = 0;
			  avg = 0;
		  }
		  public float getValue() {
			  return avg;
		  }
		  public void pushValue(float x) {
			  if(count == 0) {
				  primeBuffer(x); // first time
			  }
			  count++;
			  float lastVal = cirBuf[cirIndex];
			  avg += (x-lastVal)/cirBuf.length; // 由来:
			  cirBuf[cirIndex] = x;
			  cirIndex=nextIndex(cirIndex);
		  }
		  public long getCount() {
			  return count;
		  }
		  private void primeBuffer(float val) {
			  for(int i=0;i<cirBuf.length;i++) {
				  cirBuf[i] = val;
			  }
			  avg = val;
		  }
		  private int nextIndex(int curIndex) { // MOD(val)
			  if((curIndex + 1) >= cirBuf.length) {
				  return 0;
			  }
			  return curIndex + 1;
		  }
	  }
	  private int __mSMAk = 30;
	  private MovingAverage __mSMAx = new MovingAverage(__mSMAk); // is default
	  private MovingAverage __mSMAy = new MovingAverage(__mSMAk);
	  private MovingAverage __mSMAz = new MovingAverage(__mSMAk);
	  
	  private void MovingAverageInit() {
		  __mSMAx = new MovingAverage(__mSMAk);
		  __mSMAy = new MovingAverage(__mSMAk);
		  __mSMAz = new MovingAverage(__mSMAk);
	  }
	  public void setK(int k) {
		  __mSMAk = k;
		  MovingAverageInit();
	  }
	  public int getK() {
		 return __mSMAk; 
	  }
	  public float[] SMAFilter(float[] in) {
		  float[] out = new float[3];
		  __mSMAx.pushValue(in[0]);
		  __mSMAy.pushValue(in[1]);
		  __mSMAz.pushValue(in[2]);
		  out[0] = __mSMAx.getValue();
		  out[1] = __mSMAy.getValue();
		  out[2] = __mSMAz.getValue();
		  return out;
	  }
	  public float SMAFilter(float in) {
		  return 0;
	  }	
	}
	
	/* fir low pass filter 
	 * http://www.dspguru.com/dsp/faqs/fir/implementation
	 * 
	 * 
	 * */
	public static class firLowPassFilter{
			// constructor
		private int _NTAPS;
		private double[] h;
		private double[] h2;	// doubled-coefficient
		private double[] z;		// doubled-delay line
		private int state[] = new int[1];
		public firLowPassFilter(int hLen, double[] coe) {
			int i;
			if(hLen != coe.length) {
				//warning!
			}
			_NTAPS = coe.length;
			h = coe.clone();
			h2 = new double[2*_NTAPS];
			z = new double[2*_NTAPS];
			
			for(i=0;i<coe.length;i++) {
				h2[i] = h2[_NTAPS + i] = coe[i];
			}
			state[0] = 0;
			clear(_NTAPS, z);
		}
		public double filter1(double in) {
			return fir_double_h(in, _NTAPS, h2, z, state);
		}
		
		///////////////////////////////
		public static void clear(int ntaps, double[] h) {
			for(int i=0;i<ntaps;i++) {
				h[i] = 0.0f;
			}
		}
		public static double fir_basic(double input, int ntaps, double[] h, double[] z) {
			int i;
			double accum;
			z[0] = input;	// 最新的值放到数组开头
			accum = 0;
			for(i=0;i<ntaps;i++) {
				accum += h[i]*z[i];
			}
			for(i=ntaps-2;i>=0;i--) {	// 依次往后挪一个位置
				z[i+1] = z[i];
			}
			return accum;
		}
		public static double fir_double_h(double input, int ntaps, double[] h,double[] z, int[] state) {
			double accum;
			int i;
			int curState = state[0];
			
			z[curState] = input;
			accum = 0;
			for(i =0;i<ntaps;i++) {
				accum += h[ntaps-curState+i]*z[i];
			}
			curState--;
			if(curState < 0) {
				curState += ntaps;
			}
			state[0] = curState;
			return accum;
		}
		
		// 测试fir
		public static void testMain(String[] args) {
			int _NTAPS = 6;
			double[] h = {1.0,2.0,3.0,4.0,5.0,6.0};
			double[] h2 = new double[2*_NTAPS];
			double[] z = new double[2*_NTAPS];
			
			int IMP_SIZE = (3*_NTAPS);
			double[] imp = new double[IMP_SIZE];
			double output;
			
			int i;
			int[] state = new int[1];
			
				// a impulse input
			clear(IMP_SIZE, imp);
			imp[5] = 1.0f;
			
				//
			for(i=0;i<_NTAPS;i++) {
				h2[i] = h2[_NTAPS+i] = h[i];
			}
			
			
			System.out.printf("Testing fir_double_h: \r\n");
			clear(_NTAPS, z);
			state[0] = 0;
			for(i=0;i<IMP_SIZE;i++) {
				output = fir_double_h(imp[i], _NTAPS, h2, z, state);
				System.out.printf("[%03d] %3.11f \r\n", i, output);
			}
			System.out.printf("\r\n");
			
		}
	}
	/************************************************************/

	
	/**/
	/*
	 * true - in
	 * false - not in
	 * */
	private static boolean __inCollection(List n, float t) {
		int i;
		Float k;
		for(i=0;i<n.size();i++) {
			k = (Float)n.get(i);
			if(k.floatValue() == t) {
				return true;
			}
		}
		return false;
	}
	/*
	 * 判断数组中同不项的个数
	 * */
	public static int findDiffCount(float[] n) {
		int i;
		int c = 0;
		ArrayList<Float> list = new ArrayList<Float>();
		list.add(n[0]);
		c++;
		for(i=1;i<n.length;i++) {
			if(__inCollection(list, n[i]) == false) {
				list.add(n[i]);
				c++;
			}
		}
		return c;
	}
	/*
	 * 计算矢量长度
	 * */
	private static final double _mLimit = 0.00000001f;
	public static double vecLength(float[] n) {
		int i;
		double _sum = 0;
		for(i=0;i<n.length;i++) {
			_sum += n[i]*n[i];
		}
		if(_sum < _mLimit) {
			_sum = _mLimit;
		}
		return Math.sqrt(_sum);
	}
	public static double vecLength(double[] n) {
		int i;
		double _sum = 0;
		for(i=0;i<n.length;i++) {
			_sum += n[i]*n[i];
		}
		if(_sum < _mLimit) {
			_sum = _mLimit;
		}
		return Math.sqrt(_sum);
	}	
	public static double vecLength(float[] n, int len) {
		int i;
		double _sum = 0;
		for(i=0;i<len;i++) {
			_sum += n[i]*n[i];
		}
		if(_sum < _mLimit) {
			_sum = _mLimit;
		}
		return Math.sqrt(_sum);
	}
	public static double vecLength(double[] n, int len) {
		int i;
		double _sum = 0;
		for(i=0;i<len;i++) {
			_sum += n[i]*n[i];
		}
		if(_sum < _mLimit) {
			_sum = _mLimit;
		}
		return Math.sqrt(_sum);
	}	
	
	
  	// some utilities
  private static final Map sensorTypeNameMap = new HashMap() {
	  {
		  put(Sensor.TYPE_GRAVITY, 					"Sensor.TYPE_GRAVITY");
		  put(Sensor.TYPE_ACCELEROMETER, 			"Sensor.TYPE_ACCELEROMETER");
		  put(Sensor.TYPE_AMBIENT_TEMPERATURE, 		"Sensor.TYPE_AMBIENT_TEMPERATURE");
		  put(Sensor.TYPE_GYROSCOPE, 				"Sensor.TYPE_GYROSCOPE");
		  put(Sensor.TYPE_LIGHT, 					"Sensor.TYPE_LIGHT");
		  put(Sensor.TYPE_LINEAR_ACCELERATION, 		"Sensor.TYPE_LINEAR_ACCELERATION");
		  put(Sensor.TYPE_MAGNETIC_FIELD, 			"Sensor.TYPE_MAGNETIC_FIELD");
		  put(Sensor.TYPE_ORIENTATION, 				"Sensor.TYPE_ORIENTATION");
		  put(Sensor.TYPE_PRESSURE, 				"Sensor.TYPE_PRESSURE");
		  put(Sensor.TYPE_PROXIMITY, 				"Sensor.TYPE_PROXIMITY");
		  put(Sensor.TYPE_RELATIVE_HUMIDITY, 		"Sensor.TYPE_RELATIVE_HUMIDITY");
		  put(Sensor.TYPE_ROTATION_VECTOR, 			"Sensor.TYPE_ROTATION_VECTOR");
		  put(Sensor.TYPE_TEMPERATURE, 				"Sensor.TYPE_TEMPERATURE");
	  }
  };
  public static String getSensorTypeName(int type) {
	  return (String)sensorTypeNameMap.get(type);
  }
  private static String LOG_TAG = "Prize";
  public static void printAllSensorTypeValue() {
	  PrizeLogs.v(LOG_TAG, "Sensor.TYPE_ACCELEROMETER = " + String.valueOf(Sensor.TYPE_ACCELEROMETER));
	  PrizeLogs.v(LOG_TAG, "Sensor.TYPE_GYROSCOPE = " + String.valueOf(Sensor.TYPE_GYROSCOPE));
	  PrizeLogs.v(LOG_TAG, "Sensor.TYPE_MAGNETIC_FIELD = " + String.valueOf(Sensor.TYPE_MAGNETIC_FIELD));
	  PrizeLogs.v(LOG_TAG, "Sensor.TYPE_PRESSURE = " + String.valueOf(Sensor.TYPE_PRESSURE));
	  PrizeLogs.v(LOG_TAG, "Sensor.TYPE_RELATIVE_HUMIDITY = " + String.valueOf(Sensor.TYPE_RELATIVE_HUMIDITY));
	  PrizeLogs.v(LOG_TAG, "Sensor.TYPE_AMBIENT_TEMPERATURE = " + String.valueOf(Sensor.TYPE_AMBIENT_TEMPERATURE));
	  PrizeLogs.v(LOG_TAG, "Sensor.TYPE_LIGHT = " + String.valueOf(Sensor.TYPE_LIGHT));
	  PrizeLogs.v(LOG_TAG, "Sensor.TYPE_PROXIMITY = " + String.valueOf(Sensor.TYPE_PROXIMITY));
	  
	  PrizeLogs.v(LOG_TAG, "Sensor.TYPE_GRAVITY = " + String.valueOf(Sensor.TYPE_GRAVITY));
	  PrizeLogs.v(LOG_TAG, "Sensor.TYPE_LINEAR_ACCELERATION = " + String.valueOf(Sensor.TYPE_LINEAR_ACCELERATION));
	  PrizeLogs.v(LOG_TAG, "Sensor.TYPE_ROTATION_VECTOR = " + String.valueOf(Sensor.TYPE_ROTATION_VECTOR));
	  
	  PrizeLogs.v(LOG_TAG, "Sensor.TYPE_ORIENTATION = " + String.valueOf(Sensor.TYPE_ORIENTATION));
	  PrizeLogs.v(LOG_TAG, "Sensor.TYPE_TEMPERATURE = " + String.valueOf(Sensor.TYPE_TEMPERATURE));
  }
  public static void printAllSensorInfo(Context context) {
	  SensorManager sensorManager = 
			  (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
	  List<Sensor> allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
		for(int i=0;i<allSensors.size();i++) {
			Sensor curSensor = allSensors.get(i);
			
			int sensorType = curSensor.getType();
			String sensorName = curSensor.getName();
			String sensorVendor = curSensor.getVendor();
			int sensorVer = curSensor.getVersion();
			float sensorMaxRange = curSensor.getMaximumRange();
			int sensorMinDelay = curSensor.getMinDelay();
			float sensorPower = curSensor.getPower();
			float sensorRes = curSensor.getResolution();
			
			PrizeLogs.v(LOG_TAG, "------------------------------ [" + String.valueOf(i));
			PrizeLogs.v(LOG_TAG, "type: " + String.valueOf(sensorType) + " >" + getSensorTypeName(sensorType));
			PrizeLogs.v(LOG_TAG, "name: " + sensorName);
			PrizeLogs.v(LOG_TAG, "vendor: " + sensorVendor);
			PrizeLogs.v(LOG_TAG, "version: " + String.valueOf(sensorVer));
			PrizeLogs.v(LOG_TAG, "maxRange: " + String.valueOf(sensorMaxRange));
			PrizeLogs.v(LOG_TAG, "minDelay: " + String.valueOf(sensorMinDelay));
			PrizeLogs.v(LOG_TAG, "power: " + String.valueOf(sensorPower));
			PrizeLogs.v(LOG_TAG, "resolution: " + String.valueOf(sensorRes));
		}
  }

	// 间隔50次,连续打印5次
	private static final int __mPrintData0Delay = 50;
	private static final int __mPrintData0Delay2 = 5;
	
	private static int __mPrintData0Cnt = 0;
	private static int __mPrintData0Cnt2 = 0;
	private static boolean __mPrintData0Flag = false;
	private static long __mPrintData0LastTime = 0;
	public static void printAccleData0(float[] data) {
		  if(__mPrintData0Flag) {
			  __mPrintData0Cnt2++;
			  if(__mPrintData0Cnt2 >= __mPrintData0Delay2) {
				  __mPrintData0Cnt2 = 0;
				  __mPrintData0Flag = false;
			  }
			  
			  long curTime = System.currentTimeMillis();
			  long diffTime = curTime - __mPrintData0LastTime;
			  
	//		  Log.v(LOG_TAG, "TSP="+String.valueOf(curTime)
	//			+ ", x= " + String.valueOf(data[0])
	//			+ ", y= " + String.valueOf(data[1])
	//			+ ", z= " + String.valueOf(data[2])
	//			+ ", diffTime=" + String.valueOf(diffTime));
			  
			  System.out.printf("TSP=%010d x=%2.2f y=%2.2f z=%2.2f diffTime=%04d\r\n",
				curTime, data[0], data[1], data[2], diffTime);
			  
			  __mPrintData0LastTime = curTime;
			  return ;
		  }
		  __mPrintData0Cnt++;
		  if(__mPrintData0Cnt >= __mPrintData0Delay) {
			  __mPrintData0Cnt = 0;
			  __mPrintData0Cnt2 = 0;
			  __mPrintData0Flag = true;
			  __mPrintData0LastTime = System.currentTimeMillis();
			  Log.v(LOG_TAG,"------------------- new:");
		  }
	}
	
	public static void checksNull(Object obj, String name) {
		if(obj == null) {
			PrizeLogs.v(TAG, name + " is null");
		} else {
			PrizeLogs.v(TAG, name + " is not null");
		}
	}
	
	/* 算术平均, 不能溢出的! */
	public static double get_average(double[] buf) {
		double a = 0;
		for(int i=0;i<buf.length;i++) { // simply add together!
			a += buf[i];
		}
		a /= buf.length;
		return a;
	}
	/* 标准差 */
	public static double get_std_deviation(double[] buf) {
		double sd = 0;
		double avg = get_average(buf);
		double diff;
		double avg2 = 0;
		for(int i=0;i<buf.length;i++) {
			diff = Math.abs(buf[i] - avg);
			diff = diff*diff;
			avg2 += diff;
		}
		avg2 /= buf.length;
		sd = Math.sqrt(avg2);
		return sd;
	}
	
	/* 64 */
	public static class FftImpl1 {
	    private double inFFT[][], outFFT[][];
	    private double output_pow[];
	    
	    public double input[], output[];
	    public int N = 64;
		
		/*The array length must be a power of two. The array size is [L][2],
	    where each sample is complex; array[n][0] is the real part, array[n][1] is the imaginary part of sample n.
	    */
	    public double[][] fft_1d(double[][] array) {
	        double u_r, u_i, w_r, w_i, t_r, t_i;
	        int ln, nv2, k, l, le, le1, j, ip, i, n;
	
	        n = array.length;
	        ln = (int) (Math.log((double) n) / Math.log(2) + 0.5);
	        nv2 = n / 2;
	        //输入值换位
	        j = 1;
	        for (i = 1; i < n; i++) {
	            if (i < j) {
	                t_r = array[i - 1][0];
	                t_i = array[i - 1][1];
	                array[i - 1][0] = array[j - 1][0];
	                array[i - 1][1] = array[j - 1][1];
	                array[j - 1][0] = t_r;
	                array[j - 1][1] = t_i;
	            }
	            k = nv2; // k = nv2 = n/2
	            while (k < j) {
	                j = j - k;
	                k = k / 2;
	            }
	            j = j + k;
	        } //end for i=1~n
	
	        for (l = 1; l <= ln; l++) /* loops thru stages */ {
	            le = (int) (Math.exp((double) l * Math.log(2)) + 0.5);
	            le1 = le / 2;
	            u_r = 1.0;
	            u_i = 0.0;
	            w_r = Math.cos(Math.PI / (double) le1);
	            w_i = -Math.sin(Math.PI / (double) le1);
	            for (j = 1; j <= le1; j++) /* loops thru 1/2 twiddle values per stage */ {
	                for (i = j; i <= n; i += le) /* loops thru points per 1/2 twiddle */ {
	                    ip = i + le1;
	                    t_r = array[ip - 1][0] * u_r - u_i * array[ip - 1][1];
	                    t_i = array[ip - 1][1] * u_r + u_i * array[ip - 1][0];
	
	                    array[ip - 1][0] = array[i - 1][0] - t_r;
	                    array[ip - 1][1] = array[i - 1][1] - t_i;
	
	                    array[i - 1][0] = array[i - 1][0] + t_r;
	                    array[i - 1][1] = array[i - 1][1] + t_i;
	                }
	                t_r = u_r * w_r - w_i * u_i;
	                u_i = w_r * u_i + w_i * u_r;
	                u_r = t_r;
	            }
	        } //end of  for loops thru stages
	
	        /*
	        for(i=0;i<n;i++){
		        array[i][0]=array[i][0]/(double)N;
		        array[i][1]=array[i][1]/(double)N;
			   } //2003.12.9
	         */
	        
	        return array;
	    } /* end of FFT_1d method. */
	
	    //FFT变换，时域数值输入，经过fft_ld计算得出复值，转变为幅值数组
	    public double[] fft_sy(double[] input_array) {
	
	        inFFT = new double[N][2];
	        outFFT = new double[N][2];
	        output=new double[N];
	        output_pow = new double[N];
	
	        for (int i = 0; i < N; i++) {
	            inFFT[i][0] = input_array[i];
	            inFFT[i][1] = 0;
	            
	        }
	
	        outFFT = fft_1d(inFFT); //use fft method to get the complex after invert
	
	        
	        for(int i=0;i<N;i++){
		       
		        output_pow[i] =Math.pow(outFFT[i][0],2)+Math.pow(outFFT[i][1],2); //平方和	        
		        output[i]=Math.sqrt(output_pow[i]);//开根号
		     }
	
	         
	       /*
	        double a=0,b=0;   
	         for(int i=0;i<(N/2);i++){
		        
		         if(output[i]>a){a=output[i];  b=i;}
		         
		         if(output[i]<0.0000000001){
			         System.out.println(i + "  " + "0.0");
			         }
		         else
		         
		         System.out.println(i + "  " + output[i]);
		         }  //2003.12.9
	         System.out.println(" the max pl is "+b + "  " + a);
	             */
	         
	         return output;
	    }
	}
	/**/
    private static final float ALPHA = 0.8f;
    private static float[] __old__ = new float[3];
    public static float[] highPass(float x, float y, float z) {
    	__old__[0] = ALPHA*__old__[0] + (1-ALPHA)*x;
    	__old__[1] = ALPHA*__old__[1] + (1-ALPHA)*y;
    	__old__[2] = ALPHA*__old__[2] + (1-ALPHA)*z;
    	return new float[]{
    		x-__old__[0],
    		y-__old__[1],
    		z-__old__[2]
    	};
    }
    /**/
    public static boolean isBetween(float i, float min, float max) {
    	if(i >= min && i <= max) {
    		return true;
    	}
    	return false;
    }
    public static boolean isBetween(double i, double min, double max) {
    	if(i >= min && i <= max) {
    		return true;
    	}
    	return false;
    }
    public static void arrayClear(double[] a) {
    	for(int i=0;i<a.length;i++)
    		a[i] = 0;
    }
    public static void arrayClear(float[] a) {
    	for(int i=0;i<a.length;i++)
    		a[i] = 0;
    }
    /**/
  	public static void __notify_sound(Context cntx) {
  		NotificationManager notificationManager;
		notificationManager = (NotificationManager)cntx.getSystemService(Context.NOTIFICATION_SERVICE);
	
		Notification notification = new Notification();
		notification.defaults = Notification.DEFAULT_SOUND ;
		notificationManager.notify(0, notification);
  	}
}
