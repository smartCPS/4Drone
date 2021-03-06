﻿using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using Emotiv;
using System.IO;

public class txtTest : MonoBehaviour {
	public GameObject modal;
	public Text ObjText;

	public static EmoEngine engine;
	public static int engineUserID = -1;
	public static int userCloudID = 0;
	public int num = 0;
	public EdkDll.IEE_DataChannel_t[] channelList;

	private float fireRate = 0.2f; //지연 시간 설정
	private float nextFire = 0.0f; 

	void Awake () {	
		ObjText.text = "Connecting Engine";
		engine = EmoEngine.Instance;
		ObjText.text = "Engine enable";
		engine.UserAdded += new EmoEngine.UserAddedEventHandler (UserAddedEvent);
		//engine.UserRemoved += new EmoEngine.UserRemovedEventHandler (UserRemovedEvent);
		//engine.EmoEngineConnected += new EmoEngine.EmoEngineConnectedEventHandler (EmotivConnected);
		//engine.EmoEngineDisconnected += new EmoEngine.EmoEngineDisconnectedEventHandler (EmotivDisconnected);
		ObjText.text = "Engine setting";
		engine.Connect();
		ObjText.text = "Engine ready";
	}

	// Use this for initialization
	void Start () {
		ObjText.text = "Start!";
	}
	
	// Update is called once per frame
	void Update () {
		//ObjText.text = "Update!!";
		engine.ProcessEvents ();
		recoding ();
	}

	void OnApplicationQuit() {
		engine.Disconnect();
	}

	void recoding(){
		if (Time.time > nextFire) {
			nextFire = Time.time + fireRate;
		
			//engine.ProcessEvents(100);

			if (engineUserID < 0) {
				ObjText.text = "Id is not assigned.";
				Debug.Log ("[" + num + "]" + "wrong id " + engineUserID);

			}

			double[] alpha = new double[1];
			double[] low_beta = new double[1];
			double[] high_beta = new double[1];
			double[] gamma = new double[1];
			double[] theta = new double[1];

			//for (int i = 0; i < 5; i++) {
				engine.IEE_GetAverageBandPowers ((uint)engineUserID, channelList [0], theta, alpha, low_beta, high_beta, gamma);
				ObjText.text = channelList [0] + "  " + "Theta: " + theta [0] + ", Alpha: " + alpha[0] + ", Low_beta: " + low_beta[0] + ", High_beta: " + high_beta[0] + ", Gamma: " + gamma[0] + "\n";
				engine.IEE_GetAverageBandPowers ((uint)engineUserID, channelList [1], theta, alpha, low_beta, high_beta, gamma);
				ObjText.text += channelList [1] + "  " + "Theta: " + theta [0] + ", Alpha: " + alpha[0] + ", Low_beta: " + low_beta[0] + ", High_beta: " + high_beta[0] + ", Gamma: " + gamma[0] + "\n";
				engine.IEE_GetAverageBandPowers ((uint)engineUserID, channelList [2], theta, alpha, low_beta, high_beta, gamma);
				ObjText.text += channelList [2] + "  " + "Theta: " + theta [0] + ", Alpha: " + alpha[0] + ", Low_beta: " + low_beta[0] + ", High_beta: " + high_beta[0] + ", Gamma: " + gamma[0] + "\n";
				engine.IEE_GetAverageBandPowers ((uint)engineUserID, channelList [3], theta, alpha, low_beta, high_beta, gamma);
				ObjText.text += channelList [3] + "  " + "Theta: " + theta [0] + ", Alpha: " + alpha[0] + ", Low_beta: " + low_beta[0] + ", High_beta: " + high_beta[0] + ", Gamma: " + gamma[0] + "\n";
				engine.IEE_GetAverageBandPowers ((uint)engineUserID, channelList [4], theta, alpha, low_beta, high_beta, gamma);
				ObjText.text += channelList [4] + "  " + "Theta: " + theta [0] + ", Alpha: " + alpha[0] + ", Low_beta: " + low_beta[0] + ", High_beta: " + high_beta[0] + ", Gamma: " + gamma[0] + "\n";

				//if ((theta [0] == 0.0) && (alpha [0] == 0) && (low_beta [0] == 0) && (high_beta [0] == 0) && (gamma [0] == 0))continue;
				Debug.Log ("[" + num + "] theta is  " + theta [0]);
				//Console.WriteLine(channelList[i] + "  " + "Theta: " + theta[0] + ", Alpha: " + alpha[0] + ", Low_beta: " + low_beta[0] + ", High_beta: " + high_beta[0] + ", Gamma: " + gamma[0]);
				}
		//}
	}

	void UserAddedEvent(object sender, EmoEngineEventArgs e)
	{
		ObjText.text = "User Added ";

		engineUserID = (int)e.userId;
		ObjText.text = "User Added " + (int)e.userId;

		EmoEngine.Instance.IEE_FFTSetWindowingType((uint)engineUserID, EdkDll.IEE_WindowingTypes.IEE_HAMMING);
		ObjText.text = "User Added! " + (int)e.userId;

		channelList = new EdkDll.IEE_DataChannel_t[5]
		{ EdkDll.IEE_DataChannel_t.IED_AF3,
			EdkDll.IEE_DataChannel_t.IED_AF4,
			EdkDll.IEE_DataChannel_t.IED_T7 ,
			EdkDll.IEE_DataChannel_t.IED_T8 ,
			EdkDll.IEE_DataChannel_t.IED_O1/*Pz*/ };
		ObjText.text = "User Added!! " + (int)e.userId;


	}

	void UserRemovedEvent(object sender, EmoEngineEventArgs e)
	{
		ObjText.text = "User Removed";	
	}

	void EmotivConnected(object sender, EmoEngineEventArgs e)
	{
		ObjText.text = "Connected!!";
	}

	void EmotivDisconnected(object sender, EmoEngineEventArgs e)
	{
		ObjText.text = "Disconnected :(";
	}


}
