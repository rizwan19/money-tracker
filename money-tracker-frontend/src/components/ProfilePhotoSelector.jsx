import {useEffect, useMemo, useRef} from "react";
import {Camera, Trash2, User} from "lucide-react";

const ProfilePhotoSelector = ({image, setImage}) => {
    const inputRef = useRef(null);
    const previewUrl = useMemo(() => {
        if (!image) {
            return null;
        }

        if (typeof image === "string") {
            return image;
        }

        if (image instanceof Blob) {
            return URL.createObjectURL(image);
        }

        return null;
    }, [image]);

    useEffect(() => {
        if (!previewUrl || typeof image === "string") {
            return undefined;
        }

        return () => {
            URL.revokeObjectURL(previewUrl);
        };
    }, [image, previewUrl]);

    const handleImageChange = (e) => {
        e.preventDefault();
        const file = e.target.files?.[0];

        if (!file) {
            return;
        }

        setImage(file);
    };

    const handleImageRemove = (e) => {
        e.stopPropagation();
        setImage(null);

        if (inputRef.current) {
            inputRef.current.value = "";
        }
    };

    const onChooseFile = (e) => {
        e.preventDefault();
        inputRef.current?.click();
    };

    return (
        <div className="flex justify-center mb-6">
            <input
                type="file"
                accept="image/*"
                ref={inputRef}
                onChange={handleImageChange}
                className="hidden"
            />

            {!previewUrl ? (
                <div className="relative flex h-24 w-24 items-center justify-center rounded-full border-2 border-dashed border-slate-300 bg-slate-100">
                    <User className="text-slate-400" size={40} />
                    <button
                        type="button"
                        onClick={onChooseFile}
                        className="absolute -bottom-1 -right-1 flex h-9 w-9 items-center justify-center rounded-full bg-blue-600 text-white shadow-md transition hover:bg-blue-700"
                        aria-label="Upload profile photo"
                    >
                        <Camera size={16} />
                    </button>
                </div>
            ) : (
                <div className="relative">
                    <img
                        src={previewUrl}
                        alt="Profile preview"
                        className="h-24 w-24 rounded-full border border-slate-200 object-cover shadow-sm"
                    />
                    <button
                        type="button"
                        onClick={handleImageRemove}
                        className="absolute -bottom-1 -right-1 flex h-9 w-9 items-center justify-center rounded-full bg-red-600 text-white shadow-md transition hover:bg-red-700"
                        aria-label="Remove profile photo"
                    >
                        <Trash2 size={16} />
                    </button>
                </div>
            )}
        </div>
    );
};

export default ProfilePhotoSelector;
