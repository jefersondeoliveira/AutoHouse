package com.unipam.autohouse;

import android.os.Bundle;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class FragmentMain extends Fragment {
	private Button btLeft, speakButton, btConfig, btFechaConfig,btFechaConfigTbm;
	private RelativeLayout layoutConfig;
	public static EditText eNome;
	private Button btVozBrasilOff, btVozBrasilOn, btVozUsaOn, btVozUsaOff;
	public static Integer linguagem = 1; // 0 - us 1 - pt 
	public static String nome = "";

	public static Fragment newInstance(Context context) {
		FragmentMain f = new FragmentMain();
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_main,
				null);

		btLeft = (Button) view.findViewById(R.id.bt_left);
		speakButton = (Button) view.findViewById(R.id.bt_speak);
		btConfig = (Button) view.findViewById(R.id.bt_config);
		layoutConfig = (RelativeLayout) view.findViewById(R.id.layoutConfig);
		btFechaConfig = (Button) view.findViewById(R.id.fecharConfig);
		btFechaConfigTbm = (Button) view.findViewById(R.id.fecharConfigTbm);
		eNome = (EditText) view.findViewById(R.id.nome);
		btVozBrasilOff = (Button) view.findViewById(R.id.btVozBrasilOff);
		btVozBrasilOn = (Button) view.findViewById(R.id.btVozBrasilOn);
		btVozUsaOn = (Button) view.findViewById(R.id.btVozUsaOn);
		btVozUsaOff = (Button) view.findViewById(R.id.btVozUsaOff);

		if (!((MainActivity) getActivity()).recognitionTeste()) {
			speakButton.setEnabled(false);
			Toast.makeText(getActivity(), "Recognizer not present",
					Toast.LENGTH_SHORT).show();
		}
		openDrawer();
		speak();
		openConfig();
		
		

		return view;
	}

	public void openDrawer() {
		btLeft.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				((MainActivity) getActivity()).openDrawer();
			}
		});
	}
	
	public void openConfig() {
		btConfig.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				layoutConfig.setVisibility(View.VISIBLE);
			}
		});
		btFechaConfig.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				layoutConfig.setVisibility(View.GONE);
				nome = eNome.getText().toString();
			}
		});
		btFechaConfigTbm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				layoutConfig.setVisibility(View.GONE);
				nome = eNome.getText().toString();
			}
		});
		btVozBrasilOff.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				btVozBrasilOff.setVisibility(View.GONE);
				btVozBrasilOn.setVisibility(View.VISIBLE);
				btVozUsaOn.setVisibility(View.GONE);
				btVozUsaOff.setVisibility(View.VISIBLE);
				linguagem = 1;
				((MainActivity) getActivity()).setaLinguagem();
			}
		});
		
		btVozUsaOff.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				btVozBrasilOff.setVisibility(View.VISIBLE);
				btVozBrasilOn.setVisibility(View.GONE);
				btVozUsaOn.setVisibility(View.VISIBLE);
				btVozUsaOff.setVisibility(View.GONE);
				linguagem = 0;
				((MainActivity) getActivity()).setaLinguagem();
			}
		});
		
	}

	public void speak() {
		speakButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				((MainActivity) getActivity()).startVoiceRecognitionActivity();
			}
		});
	}

}
