package com.example.temperaturaumidadeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.temperaturaumidadeapp.ui.theme.TemperaturaUmidadeAppTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TemperaturaUmidadeAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherApp(db)
                }
            }
        }
    }
}

@Composable
fun WeatherApp(db: FirebaseFirestore) {
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var weatherData by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Função para buscar os dados no Firestore
    fun fetchWeather() {
        if (date.isEmpty() || time.isEmpty()) {
            errorMessage = "Data e hora são obrigatórias!"
            return
        }

        errorMessage = null
        db.collection("weather")
            .whereEqualTo("date", date)
            .whereEqualTo("time", time)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    errorMessage = "Dados não encontrados!"
                } else {
                    weatherData = documents.map { doc ->
                        mapOf(
                            "temperature" to (doc.getDouble("temperature") ?: 0.0),
                            "humidity" to (doc.getDouble("humidity") ?: 0.0)
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                errorMessage = "Erro ao buscar dados: ${exception.message}"
            }
    }

    // Layout da interface
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Consulte a Temperatura e Umidade",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Campos de entrada de data e hora
        TextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Data (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            isError = date.isEmpty() && errorMessage != null
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Hora (HH:MM)") },
            modifier = Modifier.fillMaxWidth(),
            isError = time.isEmpty() && errorMessage != null
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botão para buscar os dados
        Button(
            onClick = { fetchWeather() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Buscar Dados")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Exibe mensagens de erro, se houver
        errorMessage?.let {
            Text(
                it,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Exibe os dados encontrados
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            items(weatherData) { weather ->
                WeatherItem(
                    temperature = (weather["temperature"] as Double).toInt(),
                    humidity = (weather["humidity"] as Double).toInt()
                )
            }
        }
    }
}

@Composable
fun WeatherItem(temperature: Int, humidity: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Temperatura: $temperature°C", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 8.dp))
            Text("Umidade: $humidity%", style = MaterialTheme.typography.bodyLarge)
        }
    }
}