package com.agendaapppractica.agendaappxd.interfazUI.componentes

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuBox(

    opciones: List<String>,

    seleccionado: String,

    onSeleccionar: (String) -> Unit
) {

    var expanded by remember {

        mutableStateOf(false)
    }

    ExposedDropdownMenuBox(

        expanded = expanded,

        onExpandedChange = {

            expanded = !expanded
        }
    ) {

        OutlinedTextField(

            singleLine = true,

            value = seleccionado,

            onValueChange = {},

            readOnly = true,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor()
        )

        ExposedDropdownMenu(

            expanded = expanded,

            onDismissRequest = {

                expanded = false
            }
        ) {

            opciones.forEach {

                DropdownMenuItem(

                    text = {

                        Text(it)
                    },

                    onClick = {

                        onSeleccionar(it)

                        expanded =
                            false
                    }
                )
            }
        }
    }
}