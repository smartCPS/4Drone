using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
//using Emotiv;

public class NewBehaviourScript : MonoBehaviour {

	//public static EmoEngine engine;
	public Text myText;
	private float _time = 0.0f;

	// Use this for initialization
	void Start () {
		this.showTime ();
	}
	
	// Update is called once per frame
	void Update () {
		this._time += Time.deltaTime;
		this.showTime ();
	}

	void showTime (){
		this.GetComponent<GUIText>().text = "Time: " + _time;
	}
}
