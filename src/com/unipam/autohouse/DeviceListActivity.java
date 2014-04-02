package com.unipam.autohouse;

import java.util.Set;

import com.unipam.autohouse.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class DeviceListActivity extends Activity{

    // Retornar Inten��o adicional
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Campos dos membros
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar a janela
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);

        // Definir resultado CANCELADO se o usu�rio desistir
        setResult(Activity.RESULT_CANCELED);

        // Inicializar o bot�o para executar a descoberta de dispositivos
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        // Inicializar adaptadores matriz. Um para dispositivos j� emparelhados e
        // um para dispositivos rec�m-descobertos
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // Encontrar e configurar o ListView para dispositivos emparelhados
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Encontrar e configurar o ListView para dispositivos rec�m-descobertos
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Registre-se para as transmiss�es quando um dispositivo � descoberto
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Registre-se para as transmiss�es quando a descoberta foi conclu�da
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Obter o adaptador Bluetooth local
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Obter um conjunto de dispositivos actualmente emparelhados
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // Se houver dispositivos emparelhados, adicione cada um para os ArrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.nenhum_pareado).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Certifique-se de que n�o estamos mais fazendo a descoberta 
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Cancelar o registro ouvintes de radiodifus�o
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Comece a descobrir dispositivo com o Adaptador Bluetooth
     */
    private void doDiscovery() {

        // Indique varredura no t�tulo
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.escaneando);

        // Ligue legenda para novos dispositivos
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // Se j� estamos descobrindo, cancele nova descoberta
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Pedido de descoberta do Adaptador Bluetooth
        mBtAdapter.startDiscovery();
    }

    // O ouvinte onclick para todos os dispositivos nos ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancelar descoberta porque ja estamos prestes a conectar
            mBtAdapter.cancelDiscovery();
            
            // Obter o endere�o MAC do dispositivo, que � os �ltimos 17 caracteres na Vista
            String info = ((TextView) v).getText().toString();
            try {
            	// Tentar extrair um endere�o MAC
            	String address = info.substring(info.length() - 17);
            	
            	// Crie o resultado Inten��o e inclua o endere�o MAC
                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
                
                // Conjunto de resultados e terminar esta atividade
                setResult(Activity.RESULT_OK, intent);
                finish();
            }catch (IndexOutOfBoundsException e) {
            	// Falha na extra��o, conjunto de resultados e terminar esta atividade
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        }
    };

    // O receptor de transmiss�o que atende a dispositivos descobertos e
    // Altera o t�tulo quando a descoberta � terminada
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // Quando um dispositivo de detec��o encontra
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Obter o Dispositivo Bluetooth na Inten��o
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Se ele j� est� emparelhado, ignor�-lo, porque ele j� foi listado
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            // Quando a detec��o for conclu�da, altere o t�tulo Atividade
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.selecionar_dispositivo);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.nenhum_encontrado).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };
}
