package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import com.example.R
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.Contact
import com.example.ui.MainViewModel
import com.example.ui.Step
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TapSendApp(viewModel: MainViewModel) {
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val allContacts by viewModel.allContacts.collectAsStateWithLifecycle()
    val stackContacts by viewModel.stackContacts.collectAsStateWithLifecycle()
    val templateText by viewModel.templateText.collectAsStateWithLifecycle()
    val previewContacts by viewModel.previewContacts.collectAsStateWithLifecycle()
    val csvFileName by viewModel.csvFileName.collectAsStateWithLifecycle()
    val csvError by viewModel.csvError.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Notification toast state
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var showScreenNameInHeader by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2500)
            toastMessage = null
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WhatsAppDarkTeal)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showScreenNameInHeader = !showScreenNameInHeader }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Styled circle icon like the Design HTML
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(WhatsAppGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = "TapSend",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Personalized Campaigns",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppGreen,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // On the right: Progress Badge / Screen Name Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (showScreenNameInHeader) {
                            val screenLabel = when (currentStep) {
                                Step.UPLOAD -> "UPLOAD"
                                Step.COMPOSE -> "DRAFT"
                                Step.SEND -> "STACK"
                                Step.HISTORY -> "HISTORY"
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(WhatsAppGreen)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = screenLabel,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        val sentCount = allContacts.count { it.status == "SENT" }
                        val totalCount = allContacts.size
                        if (totalCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(WhatsAppDarkGreen)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$sentCount / $totalCount SENT",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            TapSendBottomNavigation(
                currentStep = currentStep,
                onStepSelected = { viewModel.setStep(it) },
                onResetClick = {
                    if (allContacts.isNotEmpty()) {
                        viewModel.resetAll()
                        toastMessage = "App Reset Successfully"
                    } else {
                        Toast.makeText(context, "No active lists to reset.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        },
        containerColor = ChatBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Stepper Row
                if (currentStep != Step.HISTORY) {
                    StepperHeader(currentStep = currentStep)
                }

                // Render current screen
                when (currentStep) {
                    Step.UPLOAD -> UploadScreen(
                        viewModel = viewModel,
                        previewContacts = previewContacts,
                        csvFileName = csvFileName,
                        csvError = csvError,
                        onImportConfirm = {
                            viewModel.confirmImport()
                            toastMessage = "Contacts Imported Successfully!"
                        }
                    )
                    Step.COMPOSE -> ComposeScreen(
                        viewModel = viewModel,
                        templateText = templateText,
                        firstContact = allContacts.firstOrNull(),
                        onGenerateLinks = {
                            viewModel.generateLinks()
                            toastMessage = "Links Generated! Ready to send."
                        }
                    )
                    Step.SEND -> SendScreen(
                        viewModel = viewModel,
                        stackContacts = stackContacts,
                        allContacts = allContacts,
                        templateText = templateText,
                        onReset = {
                            viewModel.resetAll()
                            toastMessage = "Ready for new list!"
                        }
                    )
                    Step.HISTORY -> HistoryScreen(
                        viewModel = viewModel,
                        toastCallback = { toastMessage = it }
                    )
                }
            }

            // Custom elegant floating toast matching screenshots
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                toastMessage?.let { msg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = TextDark),
                        shape = RoundedCornerShape(9999.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = WhatsAppGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = msg,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepperHeader(currentStep: Step) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, DividerLight, RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepIndicator(
            stepNumber = 1,
            title = "Upload",
            isActive = currentStep == Step.UPLOAD,
            isDone = currentStep > Step.UPLOAD
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .padding(horizontal = 8.dp)
                .background(if (currentStep > Step.UPLOAD) WhatsAppGreen else DividerLight)
        )
        StepIndicator(
            stepNumber = 2,
            title = "Draft",
            isActive = currentStep == Step.COMPOSE,
            isDone = currentStep > Step.COMPOSE
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .padding(horizontal = 8.dp)
                .background(if (currentStep > Step.COMPOSE) WhatsAppGreen else DividerLight)
        )
        StepIndicator(
            stepNumber = 3,
            title = "Send",
            isActive = currentStep == Step.SEND,
            isDone = false
        )
    }
}

@Composable
fun StepIndicator(stepNumber: Int, title: String, isActive: Boolean, isDone: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isDone -> WhatsAppGreen
                        isActive -> WhatsAppDarkGreen
                        else -> LightGray
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = stepNumber.toString(),
                    color = if (isActive) Color.White else TextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
        Text(
            text = title,
            color = if (isActive || isDone) TextDark else TextMuted,
            fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

@Composable
fun ColumnScope.UploadScreen(
    viewModel: MainViewModel,
    previewContacts: List<Contact>,
    csvFileName: String?,
    csvError: String?,
    onImportConfirm: () -> Unit
) {
    val context = LocalContext.current

    // Launcher for file picker
    val csvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.handleCsvUri(context, uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Welcome Banner showing the app's brand-new icon and subtitle
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, DividerLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(WhatsAppGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_icon_tapsend),
                        contentDescription = "TapSend App Icon",
                        modifier = Modifier.size(48.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "TapSend",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = WhatsAppDarkTeal
                    )
                    Text(
                        text = "One-tap bulk messaging app",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = WhatsAppDarkGreen
                    )
                }
            }
        }

        // Dropzone area
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Triggers the standard system document browser
                    csvPickerLauncher.launch(arrayOf("*/*"))
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .dashedBorder(
                        color = WhatsAppGreen,
                        strokeWidth = 2.dp,
                        dashWidth = 12.dp,
                        gapWidth = 8.dp,
                        cornerRadius = 32.dp
                    )
                    .padding(vertical = 40.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload",
                            tint = WhatsAppDarkGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "Upload Your List",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Needs 3 columns: Name, Number, Remarks\nTap here to browse your storage for a .csv file.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Display parsing error if any
        if (csvError != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AmberWarning),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = AmberText
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Import Notice",
                            fontWeight = FontWeight.Bold,
                            color = AmberText,
                            fontSize = 14.sp
                        )
                        Text(
                            text = csvError,
                            color = AmberText.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Preview Section (Shown before Format card when user selects a CSV list)
        if (previewContacts.isNotEmpty()) {
            Text(
                text = "LIST PREVIEW (${csvFileName ?: "uploaded_list.csv"})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Mini headers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LightGray)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(text = "Name", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text(text = "Number", modifier = Modifier.weight(1.2f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text(text = "Remarks", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }

                    // First 5 scrollable rows
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        previewContacts.take(5).forEach { contact ->
                            HorizontalDivider(color = DividerLight)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = contact.name,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 13.sp,
                                    color = TextDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = contact.number,
                                    modifier = Modifier.weight(1.2f),
                                    fontSize = 13.sp,
                                    color = TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = contact.remarks,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 13.sp,
                                    color = TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Cap notice if more than 5 rows
                        if (previewContacts.size > 5) {
                            HorizontalDivider(color = DividerLight)
                            Text(
                                text = "+ ${previewContacts.size - 5} more rows found",
                                color = WhatsAppDarkGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )
                        }
                    }

                    // Actions inside Card footer
                    HorizontalDivider(color = DividerLight)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.removeSelectedFile() },
                            colors = ButtonDefaults.textButtonColors(contentColor = ErrorColor)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Clear File")
                        }

                        Button(
                            onClick = onImportConfirm,
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                            shape = RoundedCornerShape(9999.dp)
                        ) {
                            Text(text = "Confirm Import", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Sample CSV Format Guide
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, DividerLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = WhatsAppDarkGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Supported CSV Columns format",
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        fontSize = 14.sp
                    )
                }
                
                Text(
                    text = "You can include any custom headers to use as variables in your message drafts (e.g. {{amount}} or {{date}}). All country codes are supported.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 16.sp
                )
                
                // Visual representation of a CSV structure
                Surface(
                    color = ChatBackground,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Header Row
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf("name", "country code", "number", "remarks", "amount").forEach { header ->
                                Text(
                                    text = header,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WhatsAppDarkTeal,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        HorizontalDivider(color = DividerLight.copy(alpha = 0.5f))
                        // Sample Row
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf("Krunal Rana", "91", "9827042001", "Express", "$150").forEach { valStr ->
                                Text(
                                    text = valStr,
                                    fontSize = 10.sp,
                                    color = TextDark,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick action helpers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.loadDemoData() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TipsAndUpdates,
                    contentDescription = null,
                    tint = WhatsAppDarkGreen,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Load demo list",
                    color = WhatsAppDarkGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Offline First • On-Device ONLY",
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        // Friendly empty prompt state when there's no selected CSV contacts loaded
        if (previewContacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Source,
                        contentDescription = null,
                        tint = TextMuted.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "No active lists loaded yet",
                        fontSize = 14.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val uriHandler = LocalUriHandler.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Credits: ",
                fontSize = 12.sp,
                color = TextMuted
            )
            Text(
                text = "Krunal Rana",
                fontSize = 12.sp,
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Bold,
                    color = WhatsAppDarkTeal
                ),
                modifier = Modifier.clickable {
                    try {
                        uriHandler.openUri("https://www.linkedin.com/in/krunal-rana/")
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            )
        }
    }
}

@Composable
fun ColumnScope.ComposeScreen(
    viewModel: MainViewModel,
    templateText: String,
    firstContact: Contact?,
    onGenerateLinks: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Message Card editor
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "YOUR MESSAGE TEMPLATE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )

                // Chips for insertion
                val availableVars by viewModel.availableVariables.collectAsStateWithLifecycle()
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(availableVars) { variable ->
                        VariableChip(label = "{{$variable}}") {
                            viewModel.insertVariable(variable)
                        }
                    }
                }

                // Standard WhatsApp style Compose Input
                OutlinedTextField(
                    value = templateText,
                    onValueChange = { viewModel.saveTemplate(it) },
                    placeholder = { Text(text = "Type your personalized template here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WhatsAppDarkGreen,
                        unfocusedBorderColor = DividerLight,
                        cursorColor = WhatsAppDarkGreen
                    ),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saves automatically on type",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Text(
                        text = "${templateText.length} characters",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Live Preview (Bubble Styling)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "LIVE PREVIEW (ROW 1)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Chat mockup container
            Card(
                colors = CardDefaults.cardColors(containerColor = ChatBackground),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Bubble row (aligned left like a received or sent chat, let's style as standard sent message)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp, topEnd = 4.dp))
                            .background(BubbleSelf)
                            .padding(12.dp)
                    ) {
                        // Render dynamically formatted text inside bubble
                        StyledTemplatePreview(template = templateText, previewContact = firstContact)

                        Spacer(modifier = Modifier.height(4.dp))

                        // Double ticks and timestamp
                        Row(
                            modifier = Modifier.align(Alignment.End),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "10:42 AM",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = Color(0xFF34B7F1),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = "This is a real-time preview of how messages render.",
                color = TextMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm generation CTA
        Button(
            onClick = onGenerateLinks,
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(text = "Generate Links", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun VariableChip(label: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(9999.dp),
        colors = CardDefaults.cardColors(containerColor = LightGray),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = null,
                tint = WhatsAppDarkGreen,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = WhatsAppDarkGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StyledTemplatePreview(template: String, previewContact: Contact?) {
    val contact = previewContact ?: Contact(name = "Krunal Rana", number = "+91 9827042001", remarks = "Express Delivery")
    
    val annotatedString = remember(template, contact) {
        buildAnnotatedString {
            var remaining = template
            val placeholders = mutableListOf(
                "{{name}}" to contact.name,
                "{{number}}" to contact.number,
                "{{remarks}}" to contact.remarks
            )
            for ((key, value) in contact.getCustomProperties()) {
                placeholders.add("{{$key}}" to value)
            }
            
            var index = 0
            while (index < remaining.length) {
                // Find nearest placeholder
                val nextMatch = placeholders
                    .map { it.first to remaining.indexOf(it.first, index, ignoreCase = true) }
                    .filter { it.second != -1 }
                    .minByOrNull { it.second }
                
                if (nextMatch != null) {
                    val (tag, pos) = nextMatch
                    val value = placeholders.first { it.first == tag }.second
                    
                    // Append preceding text
                    append(remaining.substring(index, pos))
                    
                    // Append actual formatted custom data
                    withStyle(style = SpanStyle(color = WhatsAppDarkGreen, fontWeight = FontWeight.Bold)) {
                        append(value)
                    }
                    
                    index = pos + tag.length
                } else {
                    append(remaining.substring(index))
                    break
                }
            }
        }
    }

    Text(
        text = annotatedString,
        color = TextDark,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
}

@Composable
fun ColumnScope.SendScreen(
    viewModel: MainViewModel,
    stackContacts: List<Contact>,
    allContacts: List<Contact>,
    templateText: String,
    onReset: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val totalContacts = allContacts.size
    val sentContacts = allContacts.count { it.status == "SENT" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Counter Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progress: $sentContacts of $totalContacts sent",
                fontSize = 14.sp,
                color = TextDark,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${totalContacts - sentContacts} remaining",
                fontSize = 13.sp,
                color = TextMuted,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Linear Progress bar
        LinearProgressIndicator(
            progress = { if (totalContacts > 0) sentContacts.toFloat() / totalContacts.toFloat() else 0f },
            color = WhatsAppGreen,
            trackColor = DividerLight,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(9999.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (stackContacts.isNotEmpty()) {
            // Vertically stacked visual card deck
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                // Take up to 3 cards and draw in reverse order so index 0 card draws last (sitting on top)
                val cardsToShow = stackContacts.take(3).reversed()
                cardsToShow.forEachIndexed { reversedIndex, contact ->
                    val index = stackContacts.indexOf(contact) // 0 = Top card, 1 = Next, 2 = Next

                    // Dynamic offsets and scales based on index position
                    val scale by animateFloatAsState(targetValue = 1f - (index * 0.04f))
                    val yOffset by animateDpAsState(targetValue = (index * 16).dp)
                    val alphaValue by animateFloatAsState(targetValue = if (index == 0) 1f else 0.8f - (index * 0.15f))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationY = yOffset.toPx()
                                alpha = alphaValue
                            }
                            .border(1.dp, if (index == 0) WhatsAppGreen.copy(alpha = 0.5f) else DividerLight, RoundedCornerShape(32.dp)),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (index == 0) 12.dp else 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top row details
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "CURRENT CONTACT",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextMuted,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = contact.name,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDark
                                        )
                                        Text(
                                            text = contact.number,
                                            fontSize = 14.sp,
                                            color = WhatsAppDarkGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    // Right contact icon box (matches HTML: bg-[#F0F2F5] p-2 rounded-xl text-[#075E54])
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(LightGray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = WhatsAppDarkTeal,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                if (contact.remarks.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Remarks: ${contact.remarks}",
                                        fontSize = 13.sp,
                                        color = TextMuted,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }

                            // Dynamic styled preview of text inside the Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ChatBackground.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Personalized text:",
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    StyledTemplatePreview(template = templateText, previewContact = contact)
                                }
                            }

                            // Bottom actions (only click actions enabled for the top visible card!)
                            if (index == 0) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Send Button (Full width, rounded-2xl)
                                    Button(
                                        onClick = {
                                            // Register pending ID so it auto-sent marks on activity resume
                                            viewModel.setPendingSentContact(contact.id)
                                            // Construct deep link and open WhatsApp
                                            launchWhatsApp(
                                                context = context,
                                                phoneNumber = contact.number,
                                                messageText = renderMessage(templateText, contact)
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Send on WhatsApp", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                    }

                                    // Send SMS Button
                                    Button(
                                        onClick = {
                                            // Register pending ID so it auto-sent marks on activity resume
                                            viewModel.setPendingSentContact(contact.id)
                                            // Launch native SMS intent
                                            launchSms(
                                                context = context,
                                                phoneNumber = contact.number,
                                                messageText = renderMessage(templateText, contact)
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppDarkTeal),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Sms,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Send SMS Text", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                    }

                                    // Skip to bottom button (Text link style)
                                    TextButton(
                                        onClick = { viewModel.moveToBottom(contact.id) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp)
                                    ) {
                                        Text(
                                            text = "Skip this contact",
                                            fontWeight = FontWeight.Medium,
                                            color = TextMuted,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            } else {
                                // Dummy placeholder spacing for background cards to prevent layout shifts
                                Box(modifier = Modifier.height(160.dp))
                            }
                        }
                    }
                }
            }

            Text(
                text = "💡 Tip: Tap Send to launch WhatsApp chat. On return, it auto-slides to the next contact!",
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        } else {
            // Complete state 🎉
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Success graphics using canvas decoration
                        Canvas(modifier = Modifier.size(80.dp)) {
                            drawCircle(color = WhatsAppGreen.copy(alpha = 0.15f), radius = 100f)
                            drawCircle(color = WhatsAppGreen, radius = 60f)
                        }

                        Text(
                            text = "All Done! 🎉",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "You successfully personalized and generated links for all contacts in this session. Tap below to import a new CSV list.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = onReset,
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(text = "Start New List", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// Custom Border with dash patterns support
fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp,
    dashWidth: Dp,
    gapWidth: Dp,
    cornerRadius: Dp
) = this.drawBehind {
    val density = this
    val strokeWidthPx = density.run { strokeWidth.toPx() }
    val dashWidthPx = density.run { dashWidth.toPx() }
    val gapWidthPx = density.run { gapWidth.toPx() }
    val cornerRadiusPx = density.run { cornerRadius.toPx() }

    val stroke = Stroke(
        width = strokeWidthPx,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidthPx, gapWidthPx), 0f)
    )

    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    )
}

// Logic to render text on specific template replacing placeholders
fun renderMessage(template: String, contact: Contact): String {
    var result = template
        .replace("{{name}}", contact.name, ignoreCase = true)
        .replace("{{number}}", contact.number, ignoreCase = true)
        .replace("{{remarks}}", contact.remarks, ignoreCase = true)
    
    val customMap = contact.getCustomProperties()
    for ((key, value) in customMap) {
        result = result.replace("{{$key}}", value, ignoreCase = true)
    }
    return result
}

@Composable
fun ColumnScope.HistoryScreen(
    viewModel: MainViewModel,
    toastCallback: (String) -> Unit
) {
    val campaigns by viewModel.allCampaigns.collectAsStateWithLifecycle()
    val drafts by viewModel.allSavedDrafts.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Section 1: Previous CSV Lists
        item {
            Text(
                text = "IMPORT HISTORY (${campaigns.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (campaigns.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No previously imported lists yet.",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(campaigns) { campaign ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(WhatsAppGreen.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContactPage,
                                        contentDescription = null,
                                        tint = WhatsAppDarkGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = campaign.name,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${campaign.contactsCount} contacts • ${android.text.format.DateUtils.getRelativeTimeSpanString(campaign.timestamp)}",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.deleteCampaign(campaign.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete campaign",
                                    tint = ErrorColor.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.restoreCampaign(campaign)
                                toastCallback("Campaign restored! Starting draft.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reuse This List", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 2: Previous Draft Messages
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SAVED TEMPLATES (${drafts.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (drafts.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No saved templates yet. Drafts are saved automatically.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(drafts) { draft ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(WhatsAppGreen.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EditNote,
                                        contentDescription = null,
                                        tint = WhatsAppDarkGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "Template #${draft.id}",
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    fontSize = 13.sp
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteSavedDraft(draft.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete template",
                                    tint = ErrorColor.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Display draft text in a bubble
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ChatBackground)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = draft.text,
                                fontSize = 13.sp,
                                color = TextDark,
                                lineHeight = 18.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.applySavedDraft(draft.text)
                                    viewModel.setStep(Step.COMPOSE)
                                    toastCallback("Template loaded! Ready to modify.")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Load & Modify", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}


// Formats number to numeric only and triggers deep-link
fun launchWhatsApp(context: Context, phoneNumber: String, messageText: String) {
    val cleanPhone = phoneNumber.filter { it.isDigit() }
    val encodedMessage = Uri.encode(messageText)
    val url = "https://wa.me/$cleanPhone?text=$encodedMessage"
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(url)
    }
    context.startActivity(intent)
}

fun launchSms(context: Context, phoneNumber: String, messageText: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("smsto:$phoneNumber")
        putExtra("sms_body", messageText)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("sms:$phoneNumber?body=${Uri.encode(messageText)}")
        }
        context.startActivity(fallbackIntent)
    }
}

@Composable
fun TapSendBottomNavigation(
    currentStep: Step,
    onStepSelected: (Step) -> Unit,
    onResetClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            .navigationBarsPadding()
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Upload Tab (List)
            BottomNavItem(
                icon = Icons.Default.ContactPage,
                label = "List",
                isSelected = currentStep == Step.UPLOAD,
                onClick = { onStepSelected(Step.UPLOAD) }
            )

            // Compose Tab (Draft)
            BottomNavItem(
                icon = Icons.Default.EditNote,
                label = "Draft",
                isSelected = currentStep == Step.COMPOSE,
                onClick = { onStepSelected(Step.COMPOSE) }
            )

            // Send Tab (Stack)
            BottomNavItem(
                icon = Icons.Default.FilterNone,
                label = "Stack",
                isSelected = currentStep == Step.SEND,
                onClick = { onStepSelected(Step.SEND) }
            )

            // History Tab
            BottomNavItem(
                icon = Icons.Default.History,
                label = "History",
                isSelected = currentStep == Step.HISTORY,
                onClick = { onStepSelected(Step.HISTORY) }
            )

            // Reset Tab
            BottomNavItem(
                icon = Icons.Default.RestartAlt,
                label = "Reset",
                isSelected = false,
                onClick = onResetClick
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .background(if (isSelected) WhatsAppGreen.copy(alpha = 0.15f) else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) WhatsAppDarkTeal else TextMuted,
            modifier = Modifier.size(26.dp)
        )
    }
}
