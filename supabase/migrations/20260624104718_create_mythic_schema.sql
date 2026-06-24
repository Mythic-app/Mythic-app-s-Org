-- Migration: Create Mythic Platform Schema
-- Generated: 2026-06-24T10:47:18

-- 1. Enable UUID Extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. Profiles Table (linked to auth.users)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username TEXT UNIQUE NOT NULL,
    email TEXT NOT NULL,
    full_name TEXT,
    avatar_url TEXT,
    level INTEGER DEFAULT 1 NOT NULL,
    xp INTEGER DEFAULT 0 NOT NULL,
    streak INTEGER DEFAULT 0 NOT NULL,
    scans_count INTEGER DEFAULT 0 NOT NULL,
    badges_count INTEGER DEFAULT 0 NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Enable RLS for Profiles
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- RLS Policies for Profiles
DROP POLICY IF EXISTS "Allow public read access to profiles" ON public.profiles;
CREATE POLICY "Allow public read access to profiles" ON public.profiles
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "Allow authenticated users to update their own profile" ON public.profiles;
CREATE POLICY "Allow authenticated users to update their own profile" ON public.profiles
    FOR UPDATE USING (auth.uid() = id);

DROP POLICY IF EXISTS "Allow profiles to be created on signup" ON public.profiles;
CREATE POLICY "Allow profiles to be created on signup" ON public.profiles
    FOR INSERT WITH CHECK (auth.uid() = id);

-- 3. Badges Table
CREATE TABLE IF NOT EXISTS public.badges (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE NOT NULL,
    code TEXT NOT NULL, -- 'explorer', 'guardian', etc.
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    tier TEXT NOT NULL CHECK (tier IN ('bronze', 'silver', 'gold', 'platinum')),
    unlocked_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    icon TEXT,
    UNIQUE (user_id, code)
);

-- Enable RLS for Badges
ALTER TABLE public.badges ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow users to read their own badges" ON public.badges;
CREATE POLICY "Allow users to read their own badges" ON public.badges
    FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Allow service role or user to insert badges" ON public.badges;
CREATE POLICY "Allow service role or user to insert badges" ON public.badges
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- 4. Quests Table
CREATE TABLE IF NOT EXISTS public.quests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE NOT NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    reward_xp INTEGER NOT NULL,
    progress INTEGER DEFAULT 0 NOT NULL,
    target INTEGER DEFAULT 1 NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('daily', 'weekly', 'achievement')),
    completed BOOLEAN DEFAULT false NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Enable RLS for Quests
ALTER TABLE public.quests ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow users to read their own quests" ON public.quests;
CREATE POLICY "Allow users to read their own quests" ON public.quests
    FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Allow users to update their own quests" ON public.quests;
CREATE POLICY "Allow users to update their own quests" ON public.quests
    FOR UPDATE USING (auth.uid() = user_id);

-- 5. Scan History Table
CREATE TABLE IF NOT EXISTS public.scan_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE NOT NULL,
    site_name TEXT NOT NULL,
    province TEXT NOT NULL,
    description TEXT NOT NULL,
    unesco_status TEXT NOT NULL,
    era TEXT NOT NULL,
    facts TEXT NOT NULL, -- JSON string or comma-separated facts
    xp_earned INTEGER DEFAULT 50 NOT NULL,
    image_url TEXT,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Enable RLS for Scan History
ALTER TABLE public.scan_history ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow users to read their own scan history" ON public.scan_history;
CREATE POLICY "Allow users to read their own scan history" ON public.scan_history
    FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Allow users to insert into scan history" ON public.scan_history;
CREATE POLICY "Allow users to insert into scan history" ON public.scan_history
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- 6. Saved Sites Table
CREATE TABLE IF NOT EXISTS public.saved_sites (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE NOT NULL,
    site_name TEXT NOT NULL,
    province TEXT NOT NULL,
    image_url TEXT,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    UNIQUE (user_id, site_name)
);

-- Enable RLS for Saved Sites
ALTER TABLE public.saved_sites ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow users to view their saved sites" ON public.saved_sites;
CREATE POLICY "Allow users to view their saved sites" ON public.saved_sites
    FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Allow users to save/unsave sites" ON public.saved_sites;
CREATE POLICY "Allow users to save/unsave sites" ON public.saved_sites
    FOR ALL USING (auth.uid() = user_id);

-- 7. Lumo Conversations Table
CREATE TABLE IF NOT EXISTS public.lumo_conversations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE NOT NULL,
    message_id TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('user', 'assistant')),
    content TEXT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Enable RLS for Lumo Conversations
ALTER TABLE public.lumo_conversations ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow users to view their conversations" ON public.lumo_conversations;
CREATE POLICY "Allow users to view their conversations" ON public.lumo_conversations
    FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Allow users to insert conversations" ON public.lumo_conversations;
CREATE POLICY "Allow users to insert conversations" ON public.lumo_conversations
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- 8. User Preferences Table
CREATE TABLE IF NOT EXISTS public.user_preferences (
    user_id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    dark_mode BOOLEAN DEFAULT true NOT NULL,
    notifications_enabled BOOLEAN DEFAULT true NOT NULL,
    language TEXT DEFAULT 'en' NOT NULL
);

-- Enable RLS for User Preferences
ALTER TABLE public.user_preferences ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow user preference access" ON public.user_preferences;
CREATE POLICY "Allow user preference access" ON public.user_preferences
    FOR ALL USING (auth.uid() = user_id);

-- 9. Automatic Profile Trigger on Signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, username, email, full_name, avatar_url, level, xp, streak, scans_count, badges_count)
    VALUES (
        new.id,
        COALESCE(new.raw_user_meta_data->>'username', split_part(new.email, '@', 1)),
        new.email,
        COALESCE(new.raw_user_meta_data->>'full_name', split_part(new.email, '@', 1)),
        COALESCE(new.raw_user_meta_data->>'avatar_url', 'https://api.dicebear.com/7.x/avataaars/svg?seed=' || new.id),
        1,
        0,
        1,
        0,
        0
    );
    
    INSERT INTO public.user_preferences (user_id, dark_mode, notifications_enabled, language)
    VALUES (new.id, true, true, 'en');
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();
