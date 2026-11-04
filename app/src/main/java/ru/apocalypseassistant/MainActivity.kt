package ru.apocalypseassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AssistantScreen() }
    }
}

@Composable
private fun AssistantScreen() {
    val activities = remember {
        mutableStateListOf(
            GameActivity("blitz", "Blitz", CostStatus.UNKNOWN, ActivityStatus.UNKNOWN, "Цена и условия ещё не проверены"),
            GameActivity("daily", "Ежедневные задания", CostStatus.UNKNOWN, ActivityStatus.UNKNOWN, "Нужно проверить бесплатность каждого шага"),
            GameActivity("afk", "AFK-награда", CostStatus.UNKNOWN, ActivityStatus.UNKNOWN, "Нужно проверить лимит и кнопку получения"),
            GameActivity("dungeon", "Подземелье", CostStatus.UNKNOWN, ActivityStatus.UNKNOWN, "Вход и повторный вход не проверены"),
            GameActivity("boss", "Босс", CostStatus.UNKNOWN, ActivityStatus.UNKNOWN, "Нужно проверить стоимость попытки"),
            GameActivity("summon", "Призыв", CostStatus.UNKNOWN, ActivityStatus.UNKNOWN, "Разрешать только отдельный бесплатный призыв")
        )
    }
    var protection by remember { mutableStateOf(true) }
    var planning by remember { mutableStateOf(false) }
    val safeCandidates = if (protection && planning) ActivityPlanner.plan(activities) else emptyList()

    MaterialTheme {
        Scaffold { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Помощник Апокалипсиса", style = MaterialTheme.typography.headlineSmall)
                    Text("Прототип 0.3.0 • безопасный планировщик")
                }
                item {
                    Card {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Защита валют", style = MaterialTheme.typography.titleMedium)
                                Switch(checked = protection, onCheckedChange = { protection = it })
                            }
                            Text("Неизвестная цена = запрет. Покупки и расход ресурсов не автоматизируются.")
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Планирование активностей")
                                Switch(checked = planning, onCheckedChange = { planning = it })
                            }
                            Text(
                                if (planning) "Создаётся только список кандидатов. Игровые нажатия отключены."
                                else "Планирование выключено."
                            )
                        }
                    }
                }
                item { Text("Активности", style = MaterialTheme.typography.titleLarge) }
                items(activities, key = { it.id }) { activity ->
                    Card {
                        Column(
                            Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(activity.title, style = MaterialTheme.typography.titleMedium)
                            Text("Стоимость: ${activity.cost} • Состояние: ${activity.status}")
                            Text(activity.note, style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    val index = activities.indexOfFirst { it.id == activity.id }
                                    if (index >= 0) {
                                        activities[index] = activity.copy(cost = CostStatus.FREE_CONFIRMED)
                                    }
                                }) {
                                    Text("Отметить: бесплатно")
                                }
                                OutlinedButton(onClick = {
                                    val index = activities.indexOfFirst { it.id == activity.id }
                                    if (index >= 0) {
                                        activities[index] = activity.copy(status = ActivityStatus.READY)
                                    }
                                }) {
                                    Text("Готово")
                                }
                            }
                        }
                    }
                }
                item {
                    Card {
                        Column(
                            Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Безопасный план", style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (protection) "Защита валют включена."
                                else "ВНИМАНИЕ: защита выключена — список кандидатов заблокирован."
                            )
                            Text("Подходящих активностей: ${safeCandidates.size}")
                            safeCandidates.forEach { candidate ->
                                Text("• ${candidate.activityId}: ${candidate.explanation}")
                            }
                            Text(
                                "Важно: ручная отметка бесплатности не подтверждает реальную цену в игре."
                            )
                        }
                    }
                }
                item {
                    Text(
                        "Следующий этап: наблюдение за экраном через системное разрешение Android. " +
                            "Никаких игровых нажатий без надёжного распознавания цены и проверки баланса."
                    )
                }
            }
        }
    }
}
