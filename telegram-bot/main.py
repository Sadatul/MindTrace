import os
import requests
from dotenv import load_dotenv
from telegram import Update
from telegram.ext import ApplicationBuilder, CommandHandler, ContextTypes

# Load environment variables from .env file
load_dotenv()
BOT_TOKEN = os.getenv("BOT_TOKEN")
BACKEND_URL = os.getenv("BACKEND_URL")

async def start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    chat_id = update.effective_chat.id
    user = update.effective_user
    args = context.args  # This holds the user_id passed in /start <user_id>

    user_id = args[0] if args else None

    if user_id:
        payload = {
            "uuid": user_id,
            "chatId": str(chat_id),
        }
        print(f"Received payload: {payload}")

        try:
            response = requests.post(BACKEND_URL, json=payload)
            response.raise_for_status()
            # await update.message.reply_text(f"Hello {user.first_name}, you’ve been registered ✅")
        except requests.RequestException as e:
            print("Backend error:", e)
            await update.message.reply_text("Something went wrong while registering. Please try again.")
    else:
        await update.message.reply_text("Missing user ID. Please use the correct link.")

def main():
    if not BOT_TOKEN:
        raise ValueError("BOT_TOKEN not found in environment.")

    app = ApplicationBuilder().token(BOT_TOKEN).build()
    app.add_handler(CommandHandler("start", start))

    print("Bot is running...")
    app.run_polling()

if __name__ == "__main__":
    main()
