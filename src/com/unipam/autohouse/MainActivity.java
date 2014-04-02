package com.unipam.autohouse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.unipam.autohouse.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements TextToSpeech.OnInitListener {
	private DrawerLayout drawer;
	private RelativeLayout navList;
	final String[] data = { "one", "two", "three" };
	final String[] fragments = { "com.unipam.autohouse.FragmentMain" };

	/* VARIAVEIS BLUETOOTH */
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private boolean connectStat = false;
	protected static final int MOVE_TIME = 80;
	OnClickListener myClickListener;
	ProgressDialog myProgressDialog;
	private Toast failToast;
	private Handler mHandler;
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private ConnectThread mConnectThread = null;
	private String deviceAddress = null;
	private static final UUID SPP_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	/* VARIAVEIS BLUETOOTH */
	private Button bluetoothOff, bluetoothOn, offGeral, offEntrada, offBar,
			offEscada, offControle, offRelogio, offRestaurante, offFesta,
			onGeral, onEntrada, onBar, onEscada, onControle, onRelogio,
			onRestaurante, onFesta;

	/* VARIAVEIS SPEAK */
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	public static ArrayList<String> matches;
	private static final String TAG = "TextToSpeechDemo";
    private TextToSpeech mTts;
    private String nome;
    private Integer linguagem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		navList = (RelativeLayout) findViewById(R.id.drawer);
		bluetoothOff = (Button) findViewById(R.id.bt_off_bluetooth);
		bluetoothOn = (Button) findViewById(R.id.bt_on_bluetooth);
		offGeral = (Button) findViewById(R.id.bt_off_geral);
		offEntrada = (Button) findViewById(R.id.bt_off_entrada);
		offBar = (Button) findViewById(R.id.bt_off_bar);
		offEscada = (Button) findViewById(R.id.bt_off_escadaria);
		offControle = (Button) findViewById(R.id.bt_off_controle);
		offRelogio = (Button) findViewById(R.id.bt_off_relogio);
		offRestaurante = (Button) findViewById(R.id.bt_off_restaunte);
		offFesta = (Button) findViewById(R.id.bt_off_festa);
		onGeral = (Button) findViewById(R.id.bt_on_geral);
		onEntrada = (Button) findViewById(R.id.bt_on_entrada);
		onBar = (Button) findViewById(R.id.bt_on_bar);
		onEscada = (Button) findViewById(R.id.bt_on_escadaria);
		onControle = (Button) findViewById(R.id.bt_on_controle);
		onRelogio = (Button) findViewById(R.id.bt_on_relogio);
		onRestaurante = (Button) findViewById(R.id.bt_on_restaunte);
		onFesta = (Button) findViewById(R.id.bt_on_festa);
		
		mTts = new TextToSpeech(this,
	            this  // TextToSpeech.OnInitListener
	            );

		FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
		tx.replace(R.id.main,
				Fragment.instantiate(MainActivity.this, fragments[0]));
		tx.commit();

		myProgressDialog = new ProgressDialog(this);
		failToast = Toast.makeText(this, R.string.falha_ao_conectar,
				Toast.LENGTH_SHORT);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (myProgressDialog.isShowing()) {
					myProgressDialog.dismiss();
				}
				// Verifique se a conexão Bluetooth foi feito para o dispositivo
				// selecionado
				if (msg.what == 1) {
					connectStat = true;
				} else {
					failToast.show();
				}
			}
		};

		// Verifique se existe adaptador bluetooth
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.bt_nao_disponivel, Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		}

		// Se a BT não estiver ligado, solicitar que seja ativado.
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}

		// Conectar para o Módulo Bluetooth
		bluetoothOn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				disconnect();
			}
		});

		bluetoothOff.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				connect();
			}
		});

		// Liga e desliga TUDO
		offGeral.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ligaGeral();
			}
		});
		onGeral.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				desligaGeral();
			}
		});

		// Liga e desliga ENTRADA
		offEntrada.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ligaEntrada();
			}
		});
		onEntrada.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				desligaEntrada();
			}
		});

		// Liga e desliga BAR
		offBar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ligaBar();
			}
		});
		onBar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				desligaBar();
			}
		});

		// Liga e desliga ESCADAS
		offEscada.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ligaEscada();
			}
		});
		onEscada.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				desligaEscada();
			}
		});

		// Liga e desliga CONTROLE
		offControle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ligaControle();
			}
		});
		onControle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				desligaControle();
			}
		});

		// Liga e desliga RELOGIO
		offRelogio.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ligaRelogio();
			}
		});
		onRelogio.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				desligaRelogio();
			}
		});

		// Liga e desliga RESTAURANTE
		offRestaurante.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ligaRestaurante();
			}
		});
		onRestaurante.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				desligaRestaurante();
			}
		});

		// Liga e desliga FESTA
		offFesta.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ligaFesta();
			}
		});
		onFesta.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				desligaFesta();
			}
		});

	}
	
	public void ligaGeral(){
		desligaFesta();
		offGeral.setVisibility(Button.GONE);
		offEntrada.setVisibility(Button.GONE);
		offBar.setVisibility(Button.GONE);
		offEscada.setVisibility(Button.GONE);
		offControle.setVisibility(Button.GONE);
		offRelogio.setVisibility(Button.GONE);
		offRestaurante.setVisibility(Button.GONE);
		onGeral.setVisibility(Button.VISIBLE);
		onEntrada.setVisibility(Button.VISIBLE);
		onBar.setVisibility(Button.VISIBLE);
		onEscada.setVisibility(Button.VISIBLE);
		onControle.setVisibility(Button.VISIBLE);
		onRelogio.setVisibility(Button.VISIBLE);
		onRestaurante.setVisibility(Button.VISIBLE);
		if (outStream != null) {
			final String conteudo = "t";
			Thread sender = new Thread() {
				public void run() {
					byte content[] = conteudo.getBytes();
					try {
						outStream.write(content.length);
						outStream.write(content);
					} catch (IOException e) {
					}
				}
			};
			sender.start();
		}
	}
	public void desligaGeral(){
		onGeral.setVisibility(Button.GONE);
		onEntrada.setVisibility(Button.GONE);
		onBar.setVisibility(Button.GONE);
		onEscada.setVisibility(Button.GONE);
		onControle.setVisibility(Button.GONE);
		onRelogio.setVisibility(Button.GONE);
		onRestaurante.setVisibility(Button.GONE);
		offGeral.setVisibility(Button.VISIBLE);
		offEntrada.setVisibility(Button.VISIBLE);
		offBar.setVisibility(Button.VISIBLE);
		offEscada.setVisibility(Button.VISIBLE);
		offControle.setVisibility(Button.VISIBLE);
		offRelogio.setVisibility(Button.VISIBLE);
		offRestaurante.setVisibility(Button.VISIBLE);
		desligaFesta();
		if (outStream != null) {
			final String conteudo = "r";
			Thread sender = new Thread() {
				public void run() {
					byte content[] = conteudo.getBytes();
					try {
						outStream.write(content.length);
						outStream.write(content);
					} catch (IOException e) {
					}
				}
			};
			sender.start();
		}
	}
	
	public void ligaEntrada(){
		desligaFesta();
		offEntrada.setVisibility(Button.GONE);
		onEntrada.setVisibility(Button.VISIBLE);
		if (outStream != null) {
			final String conteudo = "a";
			Thread sender = new Thread() {
				public void run() {
					byte content[] = conteudo.getBytes();
					try {
						outStream.write(content.length);
						outStream.write(content);
					} catch (IOException e) {
					}
				}
			};
			sender.start();
		}
	}
	public void desligaEntrada(){
		onEntrada.setVisibility(Button.GONE);
		offEntrada.setVisibility(Button.VISIBLE);
		if (outStream != null) {
			final String conteudo = "b";
			Thread sender = new Thread() {
				public void run() {
					byte content[] = conteudo.getBytes();
					try {
						outStream.write(content.length);
						outStream.write(content);
					} catch (IOException e) {
					}
				}
			};
			sender.start();
		}
	}

	public void ligaBar() {
		desligaFesta();
		offBar.setVisibility(Button.GONE);
		onBar.setVisibility(Button.VISIBLE);
		if (outStream != null) {
			final String conteudo = "c";
			Thread sender = new Thread() {
				public void run() {
					byte content[] = conteudo.getBytes();
					try {
						outStream.write(content.length);
						outStream.write(content);
					} catch (IOException e) {
					}
				}
			};
			sender.start();
		}
	}

	public void desligaBar() {
		onBar.setVisibility(Button.GONE);
		offBar.setVisibility(Button.VISIBLE);
		if (outStream != null) {
			final String conteudo = "d";
			Thread sender = new Thread() {
				public void run() {
					byte content[] = conteudo.getBytes();
					try {
						outStream.write(content.length);
						outStream.write(content);
					} catch (IOException e) {
					}
				}
			};
			sender.start();
		}
	}

	public void ligaEscada() {
		desligaFesta();
		offEscada.setVisibility(Button.GONE);
		onEscada.setVisibility(Button.VISIBLE);
		if (outStream != null) {
			final String conteudo = "e";
			Thread sender = new Thread() {
				public void run() {
					byte content[] = conteudo.getBytes();
					try {
						outStream.write(content.length);
						outStream.write(content);
					} catch (IOException e) {
					}
				}
			};
			sender.start();
		}
	}

	public void desligaEscada() {
		onEscada.setVisibility(Button.GONE);
		offEscada.setVisibility(Button.VISIBLE);
		if (outStream != null) {
			final String conteudo = "f";
			Thread sender = new Thread() {
				public void run() {
					byte content[] = conteudo.getBytes();
					try {
						outStream.write(content.length);
						outStream.write(content);
					} catch (IOException e) {
					}
				}
			};
			sender.start();
		}
	}

	public void ligaControle() {
		desligaFesta();
		offControle.setVisibility(Button.GONE);
		onControle.setVisibility(Button.VISIBLE);
		if (outStream != null) {
			final String conteudo = "g";
			Thread sender = new Thread() {
				public void run() {
					byte content[] = conteudo.getBytes();
					try {
						outStream.write(content.length);
						outStream.write(content);
					} catch (IOException e) {
					}
				}
			};
			sender.start();
		}
	}

	public void desligaControle() {
		onControle.setVisibility(Button.GONE);
		offControle.setVisibility(Button.VISIBLE);
		if (outStream != null) {
			final String conteudo = "h";
			Thread sender = new Thread() {
				public void run() {
					byte content[] = conteudo.getBytes();
					try {
						outStream.write(content.length);
						outStream.write(content);
					} catch (IOException e) {
					}
				}
			};
			sender.start();
		}
	}

	public void ligaRelogio() {
		desligaFesta();
		offRelogio.setVisibility(Button.GONE);
		onRelogio.setVisibility(Button.VISIBLE);
		if (outStream != null) {
			final String conteudo = "i";
			Thread sender = new Thread() {
				public void run() {
					byte content[] = conteudo.getBytes();
					try {
						outStream.write(content.length);
						outStream.write(content);
					} catch (IOException e) {
					}
				}
			};
			sender.start();
		}
	}

	public void desligaRelogio() {
		onRelogio.setVisibility(Button.GONE);
		offRelogio.setVisibility(Button.VISIBLE);
		if (outStream != null) {
			final String conteudo = "j";
			Thread sender = new Thread() {
				public void run() {
					byte content[] = conteudo.getBytes();
					try {
						outStream.write(content.length);
						outStream.write(content);
					} catch (IOException e) {
					}
				}
			};
			sender.start();
		}
	}

	public void ligaRestaurante() {
		desligaFesta();
		offRestaurante.setVisibility(Button.GONE);
		onRestaurante.setVisibility(Button.VISIBLE);
		if (outStream != null) {
			final String conteudo = "k";
			Thread sender = new Thread() {
				public void run() {
					byte content[] = conteudo.getBytes();
					try {
						outStream.write(content.length);
						outStream.write(content);
					} catch (IOException e) {
					}
				}
			};
			sender.start();
		}
	}

	public void desligaRestaurante() {
		onRestaurante.setVisibility(Button.GONE);
		offRestaurante.setVisibility(Button.VISIBLE);
		if (outStream != null) {
			final String conteudo = "l";
			Thread sender = new Thread() {
				public void run() {
					byte content[] = conteudo.getBytes();
					try {
						outStream.write(content.length);
						outStream.write(content);
					} catch (IOException e) {
					}
				}
			};
			sender.start();
		}
	}

	public void ligaFesta() {
		offFesta.setVisibility(Button.GONE);
		onFesta.setVisibility(Button.VISIBLE);
		if (outStream != null) {
			final String conteudo = "w";
			Thread sender = new Thread() {
				public void run() {
					byte content[] = conteudo.getBytes();
					try {
						outStream.write(content.length);
						outStream.write(content);
					} catch (IOException e) {
					}
				}
			};
			sender.start();
		}
	}

	public void desligaFesta() {
		onFesta.setVisibility(Button.GONE);
		offFesta.setVisibility(Button.VISIBLE);
		if (outStream != null) {
			final String conteudo = "p";
			Thread sender = new Thread() {
				public void run() {
					byte content[] = conteudo.getBytes();
					try {
						outStream.write(content.length);
						outStream.write(content);
					} catch (IOException e) {
					}
				}
			};
			sender.start();
		}
	}

	public void openDrawer() {
		drawer.openDrawer(navList);
	}

	/** necessário para se conectar a um dispositivo Bluetooth específico */
	public class ConnectThread extends Thread {
		private String address;
		private boolean connectionStatus;

		ConnectThread(String MACaddress) {
			address = MACaddress;
			connectionStatus = true;
		}

		public void run() {
			// Quando este retorna, ele vai "saber" sobre o servidor,
			// Através do seu endereço MAC.
			try {
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);

				// Precisamos de duas coisas antes que possamos conectar com
				// êxito
				// (Problemas de autenticação de lado): um endereço MAC, que nós
				// Já temos, e um canal RFCOMM.
				// Porque RFCOMM canais (portas aka) são limitados em
				// Número, o Android não permite que você usá-los diretamente;
				// Em vez de solicitar um mapeamento RFCOMM base em um serviço
				// ID. No nosso caso, vamos usar o conhecido serviço SPP
				// ID. Essa identificação está em UUID (GUID para vocês
				// Microsofties)
				// Formato. Dada a UUID, o Android vai lidar com o Mapeamento
				// para você. Geralmente, este retornará RFCOMM 1,
				// Mas nem sempre, depende do que outros serviços Bluetooth
				// Estão em uso no seu dispositivo Android.
				try {
					btSocket = device
							.createRfcommSocketToServiceRecord(SPP_UUID);
				} catch (IOException e) {
					connectionStatus = false;
				}
			} catch (IllegalArgumentException e) {
				connectionStatus = false;
			}

			// Descoberta pode estar acontecendo, por exemplo, se você estiver
			// executando um
			// 'Scan' para pesquisa de dispositivos Bluetooth do seu telefone
			// temos que chamar cancelDiscovery ().
			// você não quer que ele esteje em andamento quando
			// Uma tentativa de conexão é feita.
			mBluetoothAdapter.cancelDiscovery();

			// Bloqueio de ligação, para um cliente simples nada mais pode
			// Acontecer até que uma conexão bem-sucedida, de modo que
			// Não me importa se ele bloqueie.
			try {
				btSocket.connect();
			} catch (IOException e1) {
				try {
					btSocket.close();
				} catch (IOException e2) {
				}
			}

			// Crie um fluxo de dados para que possamos falar com o servidor.
			try {
				outStream = btSocket.getOutputStream();
			} catch (IOException e2) {
				connectionStatus = false;
			}

			// Enviar resultado final
			if (connectionStatus) {
				mHandler.sendEmptyMessage(1);
			} else {
				mHandler.sendEmptyMessage(0);
			}
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// Quando DeviceListActivity retorna com um dispositivo para
			// conectar
			if (resultCode == Activity.RESULT_OK) {
				// Mostrar diálogo "aguarde"
				myProgressDialog = ProgressDialog.show(
						this,
						getResources().getString(R.string.aguarde),
						getResources().getString(
								R.string.conectando_ao_dispositivo), true);

				// Obter o endereço MAC do dispositivo
				deviceAddress = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Conectar a um dispositivo com um endereço MAC específico
				mConnectThread = new ConnectThread(deviceAddress);
				mConnectThread.start();

			} else {
				// Falha ao recuperar endereço MAC
				Toast.makeText(this, R.string.macFalha, Toast.LENGTH_SHORT)
						.show();
				bluetoothOn.setVisibility(Button.GONE);
				bluetoothOff.setVisibility(Button.VISIBLE);
			}
			break;
		case REQUEST_ENABLE_BT:
			// Quando o pedido para permitir retornos Bluetooth
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth está ativado
			} else {
				// Usuário não ativou o Bluetooth ou ocorreu um erro
				Toast.makeText(this, R.string.bt_nao_foi_ativado,
						Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
		case VOICE_RECOGNITION_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				matches = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				Toast.makeText(this, matches.toString(), Toast.LENGTH_SHORT)
						.show();
				
				nome = FragmentMain.nome;
				linguagem = FragmentMain.linguagem;
				
				if(matches.toString().contains("liga") && !matches.toString().contains("desliga")){
					if(matches.toString().contains("restaurante")){
						ligaRestaurante();
						if(linguagem == 1){
							mTts.speak(nome+". A luz do restaurante foi ligada",TextToSpeech.QUEUE_FLUSH,null);
						}else{
							mTts.speak(nome+". Restaurant, turned on",TextToSpeech.QUEUE_FLUSH,null);
						}
					}else
					if(matches.toString().contains("bar")){
						ligaBar();
						if(linguagem == 1){
						mTts.speak(nome+". A luz do bar foi ligada",TextToSpeech.QUEUE_FLUSH,null);
						}else{
							mTts.speak(nome+". Bar, turned on",TextToSpeech.QUEUE_FLUSH,null);
						}
					}else
					if(matches.toString().contains("controle")){
						ligaControle();
						if(linguagem == 1){
						mTts.speak(nome+". A luz da sala de controle foi ligada",TextToSpeech.QUEUE_FLUSH,null);
						}else{
							mTts.speak(nome+". Control, turned on",TextToSpeech.QUEUE_FLUSH,null);
						}
					}else
					if(matches.toString().contains("entrada")){
						ligaEntrada();
						if(linguagem == 1){
						mTts.speak(nome+". A luz da entrada foi ligada",TextToSpeech.QUEUE_FLUSH,null);
						}else{
							mTts.speak(nome+". Entrance, turned on",TextToSpeech.QUEUE_FLUSH,null);
						}
					}else
					if(matches.toString().contains("escada")){
						ligaEscada();
						if(linguagem == 1){
						mTts.speak(nome+". A luz da escadaria foi ligada",TextToSpeech.QUEUE_FLUSH,null);
						}else{
							mTts.speak(nome+". Staircase, turned on",TextToSpeech.QUEUE_FLUSH,null);
						}
					}else
					if(matches.toString().contains("relógio")){
						ligaRelogio();
						if(linguagem == 1){
						mTts.speak(nome+". A luz do relógio foi ligada",TextToSpeech.QUEUE_FLUSH,null);
						}else{
							mTts.speak(nome+". Clock, turned on",TextToSpeech.QUEUE_FLUSH,null);
						}
					}else
					if(matches.toString().contains("geral")||matches.toString().contains("tudo")){
						ligaGeral();
						if(linguagem == 1){
						mTts.speak(nome+". Todas as luzes foram ligadas",TextToSpeech.QUEUE_FLUSH,null);
						}else{
							mTts.speak(nome+". All the lights were turned on",TextToSpeech.QUEUE_FLUSH,null);
						}
					}else
					if(matches.toString().contains("festa")){
						ligaFesta();
						if(linguagem == 1){
						mTts.speak(nome+". É hora da festa. Boa diversão",TextToSpeech.QUEUE_FLUSH,null);
						}else{
							mTts.speak(nome+". It's party time. good fun",TextToSpeech.QUEUE_FLUSH,null);
						}
					}else{
						if(linguagem == 1){
						mTts.speak(nome+". Desculpe, comando inválido. Tente dizer o que você quer ligar também.",TextToSpeech.QUEUE_FLUSH,null);
						}else{
							mTts.speak(nome+". Sorry, invalid command. Try to say what you want to turn on too.",TextToSpeech.QUEUE_FLUSH,null);
						}
					}
					
				}else
				
				if(matches.toString().contains("desliga")||matches.toString().contains("desligue")){
					if(matches.toString().contains("restaurante")){
						desligaRestaurante();
						if(linguagem == 1){
						mTts.speak(nome+". A luz do restaurante foi desligada",TextToSpeech.QUEUE_FLUSH,null);
						}else{
							mTts.speak(nome+". Restaurant, turned off",TextToSpeech.QUEUE_FLUSH,null);
						}
					}else
					if(matches.toString().contains("bar")){
						desligaBar();
						if(linguagem == 1){
						mTts.speak(nome+". A luz do bar foi desligada",TextToSpeech.QUEUE_FLUSH,null);
						}else{
							mTts.speak(nome+". Bar, turned off",TextToSpeech.QUEUE_FLUSH,null);
						}
					}else
					if(matches.toString().contains("controle")){
						desligaControle();
						if(linguagem == 1){
						mTts.speak(nome+". A luz da sala de controle foi desligada",TextToSpeech.QUEUE_FLUSH,null);
						}else{
							mTts.speak(nome+". Control, turned off",TextToSpeech.QUEUE_FLUSH,null);
						}
					}else
					if(matches.toString().contains("entrada")){
						desligaEntrada();
						if(linguagem == 1){
						mTts.speak(nome+". A luz da entrada foi desligada",TextToSpeech.QUEUE_FLUSH,null);
						}else{
							mTts.speak(nome+". Entrance, turned off",TextToSpeech.QUEUE_FLUSH,null);
						}
					}else
					if(matches.toString().contains("escada")){
						desligaEscada();
						if(linguagem == 1){
						mTts.speak(nome+". A luz da escadaria foi desligada",TextToSpeech.QUEUE_FLUSH,null);
						}else{
							mTts.speak(nome+".  Staircase, turned off",TextToSpeech.QUEUE_FLUSH,null);
						}
					}else
					if(matches.toString().contains("relógio")){
						desligaRelogio();
						if(linguagem == 1){
						mTts.speak(nome+". A luz do relógio foi desligada",TextToSpeech.QUEUE_FLUSH,null);
						}else{
							mTts.speak(nome+". The light the clock. was turned off",TextToSpeech.QUEUE_FLUSH,null);
						}
					}else
					if(matches.toString().contains("geral")||matches.toString().contains("tudo")){
						desligaGeral();
						if(linguagem == 1){
						mTts.speak(nome+". Todas as luzes foram desligadas.",TextToSpeech.QUEUE_FLUSH,null);
						}else{
							mTts.speak(nome+". All the lights were turned off.",TextToSpeech.QUEUE_FLUSH,null);
						}
					}else
					if(matches.toString().contains("festa")){
						desligaFesta();
						if(linguagem == 1){
						mTts.speak(nome+". Modo festa desativado. Até a proxima.",TextToSpeech.QUEUE_FLUSH,null);
						}else{
							mTts.speak(nome+". Off party mode. Until next.",TextToSpeech.QUEUE_FLUSH,null);
						}
					}else{
						if(linguagem == 1){
						mTts.speak(nome+". Desculpe, comando inválido. Tente dizer o que você quer desligar também.",TextToSpeech.QUEUE_FLUSH,null);
						}else{
							mTts.speak(nome+". Sorry, invalid command. Try to say what you want to shut down too.",TextToSpeech.QUEUE_FLUSH,null);
						}
					}
					
				}else{
					if(linguagem == 1){
					mTts.speak(nome+". Desculpe, comando inválido. Use ligar ou desligar seguindo do local.",TextToSpeech.QUEUE_FLUSH,null);
					}else{
						mTts.speak(nome+". Sorry, invalid command. Use turn on or off more the place",TextToSpeech.QUEUE_FLUSH,null);
					}
				}
				
				
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void emptyOutStream() {
		if (outStream != null) {
			try {
				outStream.flush();
			} catch (IOException e) {
			}
		}
	}

	public void connect() {
		// Inicie o DeviceListActivity para ver dispositivos e fazer varredura
		Intent serverIntent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
		bluetoothOff.setVisibility(Button.GONE);
		bluetoothOn.setVisibility(Button.VISIBLE);
	}

	public void disconnect() {
		if (outStream != null) {
			try {
				outStream.close();
				connectStat = false;
				bluetoothOn.setVisibility(Button.GONE);
				bluetoothOff.setVisibility(Button.VISIBLE);
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void onDestroy() {
		if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
		super.onDestroy();
		disconnect();
	}

	// speak

	public boolean recognitionTeste() {
		// Check to see if a recognition activity is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Fire an intent to start the speech recognition activity.
	 */
	public void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				"Speech recognition demo");
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}
	
	public void setaLinguagem(){
		Locale loc = null;
		if(FragmentMain.linguagem == 0){
    		loc = Locale.ENGLISH;
    	}else{
    		loc = new Locale("pt");
    	}
    	mTts.setLanguage(loc);
	}

	// Implements TextToSpeech.OnInitListener.
    public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            // Set preferred language to US english.
            // Note that a language may not be available, and the result will indicate this.
        	
        	Locale loc = null;
        	if(FragmentMain.linguagem == 0){
        		loc = Locale.ENGLISH;
        	}else{
        		loc = new Locale("pt");
        	}
        	
        	
            int result = mTts.setLanguage(Locale.ENGLISH);
            if(FragmentMain.linguagem == 1)
            	result = mTts.setLanguage(new Locale("pt"));
            
            // Try this someday for some interesting results.
            // int result mTts.setLanguage(Locale.FRANCE);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
               // Lanuage data is missing or the language is not supported.
                Log.e(TAG, "Language is not available.");
            } 
        } else {
            // Initialization failed.
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }

}
